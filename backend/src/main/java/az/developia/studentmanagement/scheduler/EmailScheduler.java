package az.developia.studentmanagement.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import az.developia.studentmanagement.repository.StudentRepository;

@Component
public class EmailScheduler {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StudentRepository studentRepository;

    @Scheduled(cron = "0 0 * * * *") // Hər saat başı
    public void sendStudentCountEmail() {
        Long count = studentRepository.count();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("hedefemail@gmail.com");
        message.setSubject("Tələbə Sayı");
        message.setText("Hazırda sistemdə " + count + " tələbə var.");

        mailSender.send(message);

        System.out.println("Email göndərildi. Tələbə sayı: " + count);
    }
}