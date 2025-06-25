package org.cosmetology.controller;

import lombok.extern.slf4j.Slf4j;
import org.cosmetology.model.Appointment;
import org.cosmetology.model.Skin;
import org.cosmetology.repository.AppointmentRepository;
import org.cosmetology.repository.SkinRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * входной контроллер обрабатывающий входящие запросы.
 */

@Slf4j
@Controller
public class AppointmentController {

    private final String INDEX_HTML = "index";
    private final String REDIRECT = "redirect:/";

    private final AppointmentRepository appointmentRepository;
    private final SkinRepository skinRepository;

    public AppointmentController(AppointmentRepository appointmentRepository, SkinRepository skinRepository) {
        this.appointmentRepository = appointmentRepository;
        this.skinRepository = skinRepository;
    }

    /**
     * переход на главную страницу.
     */
    @GetMapping("/")
    public String index (Model model) {
        log.info("загрузили главную страницу  сайта");
        model.addAttribute("searchPerformed", false); // Изначально поиск не производился
        return INDEX_HTML;
    }

    /**
     * отображение записей по номеру телефона.
     *
     * @param phoneNumber  номер телефона
     */
    @GetMapping("/getRecord")
    public String searchAppointmentsByPhone(@RequestParam(name = "telephone") String phoneNumber, Model model, RedirectAttributes redirectAttrs) {
        log.info("Ищем записи по телефону: {}", phoneNumber);
        List<Appointment> foundAppointments = appointmentRepository.findByTelephone(phoneNumber);
        model.addAttribute("appointments", foundAppointments);
        model.addAttribute("searchPerformed", true); // Установим признак, что поиск был произведен
        if (foundAppointments.isEmpty()){
            redirectAttrs.addFlashAttribute("message", "Запись с таким номером телефона '" + phoneNumber + "' не найдена !");
            return REDIRECT;
        }
        return INDEX_HTML; // Используем тот же шаблон
    }

    /**
     * удаление записи по id.
     *
     * @param id  id записи
     */
    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        log.info("Удаление записи с id : {}", id);
        appointmentRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("message", "Запись успешно удалена!");
        return REDIRECT;
    }

    /**
     * Переход на страницу редактирования записи.
     *
     * @param id  id записи
     */
    @GetMapping("/edit/{id}")
    public String editAppointmentForm(@PathVariable Long id, Model model) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        if (appointmentOptional.isPresent()) {
            model.addAttribute("appointment", appointmentOptional.get());
            return "edit-appointment";
        } else {
            return REDIRECT; // Перенаправляет на главную страницу, если запись не найдена
        }
    }

    /**
     * Переход на страницу добавления новой записи.
     */
    @GetMapping("/addRecord")
    public String addAppointmentForm() {
         return "add-appointment";
    }

    /**
     * добавление новой записи.
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <Map<String, Object>> addAppointment(
            @RequestParam String telephone,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam String recommend,
            @RequestParam String skinType,
            @RequestPart(value = "clientPhotos", required=false) MultipartFile[] clientPhotos // Массив файлов
    ) throws IOException {
        List<Skin> skinList = skinRepository.findBySkinType(skinType);
        Appointment appointment = new Appointment();
        appointment.setTelephone(telephone);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setRecommend (skinList.isEmpty() ? recommend : skinList.get(0).getRecommend());
        if (clientPhotos != null && clientPhotos.length > 0) {
            log.info("Количество загруженных фотографий: {}", clientPhotos.length);
            List<String> photosBase64 = new ArrayList<>();
            for (MultipartFile file : clientPhotos) {
                if (!file.isEmpty()) {
                    byte[] imageData = file.getBytes();
                    String base64Image = Base64.getEncoder().encodeToString(imageData);
                    photosBase64.add(base64Image);
                    log.info("Добавлено изображение: {}", base64Image.substring(0, 10)); // Показать начало BASE64
                }
            }
            appointment.setPhotos(photosBase64); // Присваиваем список фотографий
            log.info("Назначено количество фотографий: {}", appointment.getPhotos().size());
        } else {
            log.warn("Ни одна фотография не была загружена.");
        }
        appointmentRepository.save(appointment);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Запись успешно добавлена!");
        return ResponseEntity.ok(result);
    }

    /**
     * обновление записи по id.
     *
     * @param id  id записи
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateAppointment(
            @RequestParam Long id,
            @RequestParam String telephone,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam String recommend,
            @RequestParam(value = "deletedPhotos", required = false) List<String> deletedPhotos,
            @RequestPart(value = "clientPhotos", required=false) MultipartFile[] clientPhotos, // Массив файлов
            RedirectAttributes redirectAttrs
    ) {
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isPresent()) {
            Appointment updatedAppointment = existingAppointment.get();
            updatedAppointment.setTelephone(telephone);
            updatedAppointment.setAppointmentDate(appointmentDate);
            updatedAppointment.setRecommend(recommend);

            // ✅ Проверяем наличие выбранных фотографий для удаления
            if (!CollectionUtils.isEmpty(deletedPhotos)) {
                List<String> currentPhotos = updatedAppointment.getPhotos();
                List<String> newPhotos = new ArrayList<>();

                // Проходим по всем фотографиям и оставляем только те, что не отмечены для удаления
                for (int i = 0; i < currentPhotos.size(); i++) {
                    String photo = currentPhotos.get(i);
                    boolean shouldDelete = deletedPhotos.contains(photo);
                    if (!shouldDelete) {
                        newPhotos.add(photo);
                    }
                }
                updatedAppointment.setPhotos(newPhotos);
            }

            // ✅ Обработка новых фотографий
            if (clientPhotos != null && clientPhotos.length > 0) {
                List<String> newPhotos = Arrays.stream(clientPhotos)
                        .filter(file -> file != null && !file.isEmpty())
                        .map(file -> {
                    try {
                        return Base64.getEncoder().encodeToString(file.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                            return REDIRECT;
                        })
                        .collect(Collectors.toList());

                updatedAppointment.getPhotos().addAll(newPhotos);
            }

            appointmentRepository.save(updatedAppointment);
            redirectAttrs.addFlashAttribute("message", "Запись успешно обновлена!");
            log.info("Запись с ID {} успешно обновлена.", id);
        }
        return REDIRECT;
    }

    //    /**
//     * отображение всех записей.
//     */
//    @GetMapping("/allRecords")
//    public String appointmentsList(Model model, RedirectAttributes redirectAttrs) {
//        log.info("получили все записи");
//        List<Appointment> appointments = repository.findAll(Sort.by(Sort.Direction.DESC, "appointmentDate"));
//        model.addAttribute("appointments", appointments);
//        model.addAttribute("searchPerformed", false); // Изначально поиск не производился
//        if (appointments.isEmpty()){
//            redirectAttrs.addFlashAttribute("message", "Список записей пуст !");
//            return REDIRECT;
//        }
//        return INDEX_HTML; // Используем тот же шаблон
//    }
}