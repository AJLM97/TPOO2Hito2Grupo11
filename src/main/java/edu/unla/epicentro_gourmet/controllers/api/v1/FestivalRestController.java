package edu.unla.epicentro_gourmet.controllers.api.v1;

import edu.unla.epicentro_gourmet.services.IFestivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festival")
public class FestivalRestController {

    private final IFestivalService festivalService;

    @Autowired
    public FestivalRestController(IFestivalService festivalService) {
        this.festivalService = festivalService;
    }

}
