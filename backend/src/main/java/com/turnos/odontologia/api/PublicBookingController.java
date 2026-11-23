package com.turnos.odontologia.api;

import com.turnos.odontologia.application.booking.BookingService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import com.turnos.odontologia.api.dto.publicbooking.ClinicInfoResponse;

@RestController
@RequestMapping("/api/public/booking")
public class PublicBookingController {

    private final BookingService bookingService;

    public PublicBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{slug}")
    public ClinicInfoResponse clinicInfo(@PathVariable String slug) {
        return bookingService.getClinicInfo(slug);
    }

    @GetMapping("/{slug}/slots")
    public List<String> slots(@PathVariable String slug,
                              @RequestParam Long professionalId,
                              @RequestParam Long serviceId,
                              @RequestParam String date) {
        return bookingService.availableSlots(slug, professionalId, serviceId, LocalDate.parse(date));
    }
}
