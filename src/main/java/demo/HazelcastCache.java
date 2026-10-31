package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class HazelcastCache {

    public static void main(String[] args) {

        SessionFactory factory = HibernateUtil.getSessionFactory();

        try {
            Long libraryId;
            try (Session session = factory.openSession()) {
                Transaction transaction = session.beginTransaction();
                Library library = new Library("City Central Library", "Bengaluru", 12000);
                session.persist(library);
                transaction.commit();
                libraryId = library.getId();
                System.out.println("Saved: " + library);
            }

            // Ensure the first lookup checks the database; the second session can use Hazelcast.
            factory.getCache().evictEntityData(Library.class, libraryId);
            factory.getStatistics().clear();
            readLibrary(factory, libraryId, "First read");
            readLibrary(factory, libraryId, "Second read");
            System.out.println("Second-level cache hits: "
                    + factory.getStatistics().getSecondLevelCacheHitCount());
        } finally {
            factory.close();
        }
    }

    private static void readLibrary(SessionFactory factory, Long libraryId, String label) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            System.out.println(label + ": " + session.get(Library.class, libraryId));
            transaction.commit();
        }
    }
}
