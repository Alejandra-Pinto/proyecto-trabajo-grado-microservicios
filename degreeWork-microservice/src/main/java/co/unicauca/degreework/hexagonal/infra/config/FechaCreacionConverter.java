package co.unicauca.degreework.hexagonal.infra.config;

import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.util.Optional;

@Converter(autoApply = true)
public class FechaCreacionConverter implements AttributeConverter<FechaCreacion, LocalDate> {
    
    @Override
    public LocalDate convertToDatabaseColumn(FechaCreacion fechaCreacion) {
        return Optional.ofNullable(fechaCreacion)
                .map(FechaCreacion::getValor)
                .orElse(null);
    }

    @Override
    public FechaCreacion convertToEntityAttribute(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(FechaCreacion::createFechaCreacion)
                .orElse(null);
    }
}