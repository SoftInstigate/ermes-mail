FROM openjdk:11

LABEL maintainer="SoftInstigate <info@softinstigate.com>"

WORKDIR /opt

COPY target/ermes-mail.jar /opt
COPY template.html /opt

# MailSender Configuration Enviroment Variables
ENV SMTP_HOSTNAME="smtp.gmail.com" \
SMTP_PORT="465" \
SMTP_USERNAME="<example@gmail.com>" \
SMTP_PASSWORD="<password>" \
FROM_EMAIL="<sender-email>" 

# Sends a text email. Fill with requested params
# ENTRYPOINT [ "java", "-jar", "ermes-mail.jar", "text", "<to-email>", "<subject>", "<message>"]

# Sends an html email. Fill with requested params
# ENTRYPOINT [ "java", "-jar", "ermes-mail.jar", "html", "<to-email>", "<subject>", "<html-message>"]

# Sends an html email by using a template. Fill with requested params.
# ENTRYPOINT [ "java", "-jar", "ermes-mail.jar", "html-template", "<to-email>", "<subject>", "<html-template-filepath>", "TEMPLATE_PARAM_1:PARAM_VALUE_1,TEMPLATE_PARAM_2:PARAM_VALUE_2"]

ENTRYPOINT [ "java", "-jar", "ermes-mail.jar", "html-template", "<to-email>", "exampleSubject", "template.html", "TEMPLATE_PARAM_1:PARAM_VALUE_1,TEMPLATE_PARAM_2:PARAM_VALUE_2"]
