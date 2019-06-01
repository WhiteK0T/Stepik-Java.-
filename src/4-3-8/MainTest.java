import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.logging.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    public static final String AUSTIN_POWERS = "Austin Powers";
    public Logger logger = Logger.getLogger(Main.class.getName());

    @Test
    void testInspector_StoneTrue() {
        Main.AbstractSendable test = new Main.MailPackage("FBR", "NASA", new Main.Package("stones ", 1000));
        try {
            new Main.Inspector().processMail(test);
            fail("Не выбросило исключение StolenPackageException");
        } catch (Exception e) {
            assert (Main.StolenPackageException.class == e.getClass());
        }
    }

    @Test
    void testInspector_IllegalTrue1() {
        Main.AbstractSendable test = new Main.MailPackage("FBR", "NASA", new Main.Package("banned substance ", 1000));
        try {
            new Main.Inspector().processMail(test);
            fail("Не выбросило исключение IllegalPackageException");
        } catch (Exception e) {
            assert (Main.IllegalPackageException.class == e.getClass());
        }
    }

    @Test
    void testInspector_IllegalTrue2() {
        Main.AbstractSendable test = new Main.MailPackage("FBR", "NASA", new Main.Package("weapons substance ", 1000));
        try {
            new Main.Inspector().processMail(test);
            fail("Не выбросило исключение IllegalPackageException");
        } catch (Exception e) {
            assert (Main.IllegalPackageException.class == e.getClass());
        }
    }

    @Test
    void testInspector_TrueMailPackage() {
        Main.MailPackage test = new Main.MailPackage(AUSTIN_POWERS, "NASA", new Main.Package(" asdda", 1000));
        try {
            Main.MailPackage result = (Main.MailPackage) new Main.Inspector().processMail(test);
            assert (test.equals(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testInspector_TrueMailMessage() {
        Main.MailMessage test = new Main.MailMessage(AUSTIN_POWERS, "NASA", "AAAAA");
        try {
            Main.MailMessage result = (Main.MailMessage) new Main.Inspector().processMail(test);
            assert (test.equals(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSpy_SpyCorrectResultReturn() {
        logger.setLevel(Level.OFF);
        Main.MailPackage test = new Main.MailPackage(AUSTIN_POWERS, "NASA", new Main.Package(" asdda", 1000));
        Main.MailPackage result = (Main.MailPackage) new Main.Spy(logger).processMail(test);
        assert (test.equals(result));
    }

    class myFilter implements Filter{
        public boolean compare = false;
        private String findStr;
        private Object[] arrStr;

        public myFilter(String findStr, Object[] arrStr) {
            this.findStr = findStr;
            this.arrStr = arrStr;
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            if (record.getMessage().equals(findStr)) {
                compare = Arrays.equals(record.getParameters(), arrStr);
            }
            return false;
        }
    }

    @Test
    void testSpy_SpyCorrectLogRec1() {
        Main.MailMessage test = new Main.MailMessage(AUSTIN_POWERS, "NASA", "SOS");
        logger.setLevel(Level.WARNING);
        logger.setFilter(new myFilter("Detected target mail correspondence: from {0} to {1} \"{2}\"",
                new Object[]{"Austin Powers", "NASA", "SOS"}));
        new Main.Spy(logger).processMail(test);
        if (!((myFilter) logger.getFilter()).compare) {
            fail();
        }
    }

    @Test
    void testSpy_SpyCorrectLogRec2() {
        Main.MailMessage test = new Main.MailMessage("CCCP", "NASA", "SOS");
        logger.setLevel(Level.INFO);
        logger.setFilter(new myFilter("Usual correspondence: from {0} to {1}",
                new Object[]{"CCCP", "NASA"}));
        new Main.Spy(logger).processMail(test);
        if (!((myFilter) logger.getFilter()).compare) {
            fail();
        }
    }

    @Test
    void testThief_TheThiefSteals() {
        Main.MailPackage test = new Main.MailPackage("CCCP", "NASA", new Main.Package("asdda", 1000));
        Main.MailPackage result = (Main.MailPackage) new Main.Thief(1000).processMail(test);
        assertEquals("stones instead of asdda", result.getContent().getContent(), "Содержимое посылки некоректно!");
        assertEquals(0, result.getContent().getPrice(), "Цена не 0");
    }

    @Test
    void testThief_CorrectReturnMailMessage() {
        Main.MailMessage test = new Main.MailMessage("CCCP", "NASA", " Z ");
        Main.MailMessage result = (Main.MailMessage) new Main.Thief(1000).processMail(test);
        assertEquals(test, result);
    }

    @Test
    void testThief_CorrectReturnMailPackage() {
        Main.MailPackage test = new Main.MailPackage("CCCP", "NASA", new Main.Package("asdda", 999));
        Main.MailPackage result = (Main.MailPackage) new Main.Thief(1000).processMail(test);
        assertEquals(test, result);
    }

    @Test
    void testUntrustworthyMailWorker() {
        Main.MailMessage test = new Main.MailMessage("CCCP", "NASA", "1");
        Main.MailMessage result = (Main.MailMessage) new Main.UntrustworthyMailWorker(new Main.MailService[]{
                new testMailService(), new testMailService(), new testMailService()
        }).processMail(test);
        assertEquals(4, Integer.parseInt(result.getMessage()));
    }

    private class testMailService implements Main.MailService {
        @Override
        public Main.Sendable processMail(Main.Sendable mail) {
            int i = Integer.parseInt(((Main.MailMessage)mail).getMessage());
            return new Main.MailMessage(mail.getFrom(), mail.getTo(),String.valueOf(++i));
        }
    }
}