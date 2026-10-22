package edu.unla.epicentro_gourmet.configuration;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionModelMapper {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<SolicitudFestival, Festival> solicitudAFestival = modelMapper.createTypeMap(SolicitudFestival.class, Festival.class);
        solicitudAFestival.addMappings(mapper -> mapper.map(SolicitudFestival::getFechaInicio, Festival::setFechInicio));
        TypeMap<Festival, RespuestaFestival> festivalARespuesta = modelMapper.createTypeMap(Festival.class, RespuestaFestival.class);
        festivalARespuesta.addMappings(mapper -> mapper.map(Festival::getFechInicio, RespuestaFestival::setFechaInicio));
        return modelMapper;
    }
}