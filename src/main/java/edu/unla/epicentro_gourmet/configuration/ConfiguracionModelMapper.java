package edu.unla.epicentro_gourmet.configuration;

import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
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
        TypeMap<SolicitudFestival, Festival> festivalTypeMap =
                modelMapper.createTypeMap(SolicitudFestival.class, Festival.class);
        festivalTypeMap.addMappings(mapper -> mapper.skip(Festival::setId));
        festivalTypeMap.addMappings(mapper -> mapper.map(
                SolicitudFestival::getFechaInicio,
                Festival::setFechInicio
        ));
        TypeMap<Festival, RespuestaFestival> respuestaTypeMap =
            modelMapper.createTypeMap(Festival.class, RespuestaFestival.class);
        respuestaTypeMap.addMappings(mapper -> mapper.map(
            Festival::getFechInicio,
            RespuestaFestival::setFechaInicio
        ));
        return modelMapper;
    }
}