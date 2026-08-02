package com.softinstigate.ermes.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class EmailModelTest {

    @Test
    void constructorAssignsFieldsCorrectly() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "<p>Body</p>");

        assertEquals("a@b.com", model.from);
        assertEquals("Alice", model.senderFullName);
        assertEquals("Hello", model.subject);
        assertEquals("<p>Body</p>", model.message);
    }

    @Test
    void constructorWithNullSenderFullName() {
        EmailModel model = new EmailModel("a@b.com", null, "Hello", "Body");

        assertEquals("a@b.com", model.from);
        assertNull(model.senderFullName);
    }

    @Test
    void addToAddsRecipient() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addTo("c@d.com", "Charlie");

        List<EmailModel.Recipient> recipients = model.getToRecipients();
        assertEquals(1, recipients.size());
        assertEquals("c@d.com", recipients.get(0).email);
        assertEquals("Charlie", recipients.get(0).name);
    }

    @Test
    void addCcAddsRecipient() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addCc("c@d.com", "Charlie");

        List<EmailModel.Recipient> cc = model.getCcRecipients();
        assertEquals(1, cc.size());
        assertEquals("c@d.com", cc.get(0).email);
        assertEquals("Charlie", cc.get(0).name);
    }

    @Test
    void addBccAddsRecipient() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addBcc("c@d.com", "Charlie");

        List<EmailModel.Recipient> bcc = model.getBccRecipients();
        assertEquals(1, bcc.size());
        assertEquals("c@d.com", bcc.get(0).email);
        assertEquals("Charlie", bcc.get(0).name);
    }

    @Test
    void setMultipleToReplacesExisting() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addTo("old@x.com", "Old");
        model.setMultipleTo(List.of("new1@x.com", "new2@x.com"));

        List<EmailModel.Recipient> recipients = model.getToRecipients();
        assertEquals(2, recipients.size());
        assertEquals("new1@x.com", recipients.get(0).email);
        assertEquals("new2@x.com", recipients.get(1).email);
    }

    @Test
    void setMultipleCcReplacesExisting() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addCc("old@x.com", "Old");
        model.setMultipleCc(List.of("new1@x.com", "new2@x.com"));

        List<EmailModel.Recipient> cc = model.getCcRecipients();
        assertEquals(2, cc.size());
        assertEquals("new1@x.com", cc.get(0).email);
        assertEquals("new2@x.com", cc.get(1).email);
    }

    @Test
    void setMultipleBccReplacesExisting() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addBcc("old@x.com", "Old");
        model.setMultipleBcc(List.of("new1@x.com", "new2@x.com"));

        List<EmailModel.Recipient> bcc = model.getBccRecipients();
        assertEquals(2, bcc.size());
        assertEquals("new1@x.com", bcc.get(0).email);
        assertEquals("new2@x.com", bcc.get(1).email);
    }

    @Test
    void setToClearsAndReplaces() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addTo("old@x.com", "Old");

        model.setTo(List.of(new EmailModel.Recipient("new@x.com", "New")));

        List<EmailModel.Recipient> recipients = model.getToRecipients();
        assertEquals(1, recipients.size());
        assertEquals("new@x.com", recipients.get(0).email);
    }

    @Test
    void setCcClearsAndReplaces() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addCc("old@x.com", "Old");

        model.setCc(List.of(new EmailModel.Recipient("new@x.com", "New")));

        List<EmailModel.Recipient> cc = model.getCcRecipients();
        assertEquals(1, cc.size());
        assertEquals("new@x.com", cc.get(0).email);
    }

    @Test
    void setBccClearsAndReplaces() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addBcc("old@x.com", "Old");

        model.setBcc(List.of(new EmailModel.Recipient("new@x.com", "New")));

        List<EmailModel.Recipient> bcc = model.getBccRecipients();
        assertEquals(1, bcc.size());
        assertEquals("new@x.com", bcc.get(0).email);
    }

    @Test
    void setAttachmentsClearsAndReplaces() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addAttachment("http://old.com/f.txt", "old.txt", "Old");

        model.setAttachments(List.of(
                new EmailModel.Attachment("http://new.com/f.txt", "new.txt", "New")));

        List<EmailModel.Attachment> attachments = model.getAttachments();
        assertEquals(1, attachments.size());
        assertEquals("http://new.com/f.txt", attachments.get(0).url);
    }

    @Test
    void getToRecipientsReturnsUnmodifiable() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addTo("x@y.com", "X");

        List<EmailModel.Recipient> recipients = model.getToRecipients();
        assertThrows(UnsupportedOperationException.class, () -> recipients.add(
                new EmailModel.Recipient("z@w.com", "Z")));
    }

    @Test
    void getCcRecipientsReturnsUnmodifiable() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addCc("x@y.com", "X");

        List<EmailModel.Recipient> cc = model.getCcRecipients();
        assertThrows(UnsupportedOperationException.class, () -> cc.add(
                new EmailModel.Recipient("z@w.com", "Z")));
    }

    @Test
    void getBccRecipientsReturnsUnmodifiable() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addBcc("x@y.com", "X");

        List<EmailModel.Recipient> bcc = model.getBccRecipients();
        assertThrows(UnsupportedOperationException.class, () -> bcc.add(
                new EmailModel.Recipient("z@w.com", "Z")));
    }

    @Test
    void getAttachmentsReturnsUnmodifiable() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addAttachment("http://x.com/f.txt", "f.txt", "desc");

        List<EmailModel.Attachment> attachments = model.getAttachments();
        assertThrows(UnsupportedOperationException.class, () -> attachments.add(
                new EmailModel.Attachment("http://z.com/f.txt", "z.txt", "z")));
    }

    @Test
    void toStringRedactsMessage() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Secret body content");

        String s = model.toString();
        assertTrue(s.contains("[REDACTED]"));
        assertFalse(s.contains("Secret body content"));
    }

    @Test
    void toSecureStringReplacesSubjectWithLength() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello World", "Body");

        String s = model.toSecureString();
        assertTrue(s.contains("[11 chars]"), "Expected subject length in toSecureString, got: " + s);
        assertFalse(s.contains("Hello World"), "toSecureString should not contain actual subject");
    }

    @Test
    void toSecureStringReplacesMessageWithLength() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Sub", "A longer message body here");

        String s = model.toSecureString();
        assertTrue(s.contains("[26 chars]"), "Expected message length in toSecureString, got: " + s);
        assertFalse(s.contains("A longer message body here"));
    }

    @Test
    void toSecureStringContainsRecipientCounts() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Sub", "Body");
        model.addTo("t@x.com", "T");
        model.addTo("t2@x.com", "T2");
        model.addCc("c@x.com", "C");
        model.addBcc("b@x.com", "B");

        String s = model.toSecureString();
        assertTrue(s.contains("toCount=2"), "Expected toCount=2, got: " + s);
        assertTrue(s.contains("ccCount=1"), "Expected ccCount=1, got: " + s);
        assertTrue(s.contains("bccCount=1"), "Expected bccCount=1, got: " + s);
        assertTrue(s.contains("attachmentCount=0"), "Expected attachmentCount=0, got: " + s);
    }

    @Test
    void toSecureStringHandlesNullSubjectAndMessage() {
        EmailModel model = new EmailModel("a@b.com", "Alice", null, null);

        String s = model.toSecureString();
        assertTrue(s.contains("subject='null'"), "Expected null subject, got: " + s);
        assertTrue(s.contains("message='null'"), "Expected null message, got: " + s);
    }

    @Test
    void addAttachmentAddsToList() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Sub", "Body");
        model.addAttachment("http://example.com/file.pdf", "file.pdf", "A report");

        List<EmailModel.Attachment> attachments = model.getAttachments();
        assertEquals(1, attachments.size());
        assertEquals("http://example.com/file.pdf", attachments.get(0).url);
        assertEquals("file.pdf", attachments.get(0).fileName);
        assertEquals("A report", attachments.get(0).description);
    }

    @Test
    void setMultipleToWithEmptyList() {
        EmailModel model = new EmailModel("a@b.com", "Alice", "Hello", "Body");
        model.addTo("x@y.com", "X");
        model.setMultipleTo(List.of());

        assertTrue(model.getToRecipients().isEmpty());
    }

    @Test
    void recipientToStringFormat() {
        EmailModel.Recipient r = new EmailModel.Recipient("a@b.com", "Alice");

        String s = r.toString();
        assertTrue(s.contains("a@b.com"), "Expected email in toString, got: " + s);
        assertTrue(s.contains("Alice"), "Expected name in toString, got: " + s);
    }

    @Test
    void recipientConstructorWithNullName() {
        EmailModel.Recipient r = new EmailModel.Recipient("a@b.com", null);

        assertEquals("a@b.com", r.email);
        assertNull(r.name);
    }

    @Test
    void attachmentToStringFormat() {
        EmailModel.Attachment a = new EmailModel.Attachment("http://x.com/f.txt", "f.txt", "desc");

        String s = a.toString();
        assertTrue(s.contains("http://x.com/f.txt"), "Expected url in toString, got: " + s);
        assertTrue(s.contains("f.txt"), "Expected fileName in toString, got: " + s);
        assertTrue(s.contains("desc"), "Expected description in toString, got: " + s);
    }
}
