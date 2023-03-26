import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TrsDecisionToolEmail {
    protected static String username; // GMail username (just the part before "@gmail.com")
    protected static String password; // GMail password
    protected String recipient;
    protected String subject;
    protected String body;
    protected String sentMessage = "Email sent!";

    /**
     * Class constructor
     * @param member
     */
    public TrsDecisionToolEmail(TrsMember member, MonteCarloSimulation sim) {
        recipient = member.getEmail();
        subject = "Your TRS Decision Tool Report";
        body = "<html><body>" + member.toHtmlString() + sim.toHtmlString() + "</html></body>";
    }

    public void setRecipient(String recipient) { this.recipient = recipient; }

    /**
     * email credentials must be saved in a file called EmailCredentials.txt
     * EmailCredentials must consist of just two lines:
     * line 1: a GMail username (username only, excluding "@gmail.com")
     * line 2: the password for the GMail account
     */
    public static void setEmailCredentials() {
        try(BufferedReader in = new BufferedReader(new FileReader("EmailCredentials.txt"))) {
            username = in.readLine();
            password = in.readLine();
        }
        catch (IOException e) {
            System.out.println("File Read Error");
        }
    }

    /**
     * sends the <code>Email</code> to <code>recipient</code>
     */
    public void send() {
        setEmailCredentials();

        try {
            Properties props = System.getProperties();
            String host = "smtp.gmail.com";
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", username);
            props.put("mail.smtp.password", password);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(props);
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);

            MimeMultipart multipart = new MimeMultipart("related");

            // first part (the html)
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html");

            // add it
            multipart.addBodyPart(messageBodyPart);

            // second part (the image)
            messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource(TrsHistogram.HISTOGRAM_FILE_PATH);
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID","<image>");

            // add it
            multipart.addBodyPart(messageBodyPart);

            // put everything together
            message.setContent(multipart);

            Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.print(sentMessage);
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    public static void runSender(TrsMember member, MonteCarloSimulation simForMember) {
        Scanner scan = new Scanner(System.in);
        String iWantEmail = "";
        while (!iWantEmail.equals("N")) {
            if (TrsMember.isValidEmailAddress(member.getEmail())) {
                TrsDecisionToolEmail email = new TrsDecisionToolEmail(member, simForMember);
                email.send();
                break;
            }
            else {
                System.out.println("You haven't entered a valid email address.");
                System.out.print("Enter your email address, or enter N to decline an emailed report: ");
                iWantEmail = scan.next().trim();
                if (iWantEmail.equals("N")) {
                    System.out.println("Got it. I won't email you.");
                } else {
                    member.setEmail(iWantEmail);
                }
            }
        }
    }
}