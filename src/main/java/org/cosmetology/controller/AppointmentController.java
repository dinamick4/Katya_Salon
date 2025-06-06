package org.cosmetology.controller;

import lombok.extern.slf4j.Slf4j;
import org.cosmetology.model.Appointment;
import org.cosmetology.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class AppointmentController {

    private final String INDEX_HTML = "index";
    private final String REDIRECT = "redirect:/";

    @Autowired
    private AppointmentRepository repository;

    // Главная страница с расписанием
    @GetMapping("/")
    public String index (Model model) {
        log.info("загрузили главную страницу  сайта");
        model.addAttribute("searchPerformed", false); // Изначально поиск не производился
        return INDEX_HTML;
    }

    // Главная страница с расписанием
    @GetMapping("/allRecords")
    public String appointmentsList(Model model) {
        log.info("получили все записи");
        List<Appointment> appointments = repository.findAll(Sort.by(Sort.Direction.DESC, "appointmentDate"));
        model.addAttribute("appointments", appointments);
        model.addAttribute("searchPerformed", false); // Изначально поиск не производился
        return INDEX_HTML;
    }

    // Поиск записей по номеру телефона
    @GetMapping("/getRecord")
    public String searchAppointmentsByPhone(@RequestParam(name = "telephone") String phoneNumber, Model model) {
        log.info("Ищем записи по телефону: {}", phoneNumber);
        List<Appointment> foundAppointments = repository.findByTelephone(phoneNumber);
        model.addAttribute("appointments", foundAppointments);
        model.addAttribute("searchPerformed", true); // Установим признак, что поиск был произведен
        return INDEX_HTML; // Используем тот же шаблон
    }


    // Добавляем запись
    @PostMapping("/add")
    public String addAppointment(@RequestParam String telephone,  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate, @RequestParam String recommend, RedirectAttributes redirectAttrs) {
        Appointment appointment = new Appointment();
        appointment.setTelephone(telephone);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setRecommend(recommend);
        repository.save(appointment);
        redirectAttrs.addFlashAttribute("message", "Запись успешно добавлена!");
        return REDIRECT;
    }

    // Удаляем запись
    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        log.info("Удаление записи с id : {}", id);
        repository.deleteById(id);
        redirectAttrs.addFlashAttribute("message", "Запись успешно удалена!");
        return REDIRECT;
    }

    // Переход на страницу редактирования записи
    @GetMapping("/edit/{id}")
    public String editAppointmentForm(@PathVariable Long id, Model model) {
        Optional<Appointment> appointmentOptional = repository.findById(id);
        if (appointmentOptional.isPresent()) {
            model.addAttribute("appointment", appointmentOptional.get());
            return "edit-appointment";
        } else {
            return REDIRECT; // Перенаправляет на главную страницу, если запись не найдена
        }
    }

    // Обработчик POST-запроса для сохранения изменений
    @PostMapping("/update")
    public String updateAppointment(@RequestParam Long id,
                                    @RequestParam String telephone,
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
                                    @RequestParam String recommend,
                                    RedirectAttributes redirectAttrs) {
        Optional<Appointment> existingAppointment = repository.findById(id);
        if (existingAppointment.isPresent()) {
            Appointment updatedAppointment = existingAppointment.get();
            updatedAppointment.setTelephone(telephone);
            updatedAppointment.setAppointmentDate(appointmentDate);
            updatedAppointment.setRecommend(recommend);
            repository.save(updatedAppointment);
            redirectAttrs.addFlashAttribute("message", "Запись успешно обновлена!");
            log.info("Запись с ID {} успешно обновлена.", id);
        }
        return REDIRECT;
    }
}