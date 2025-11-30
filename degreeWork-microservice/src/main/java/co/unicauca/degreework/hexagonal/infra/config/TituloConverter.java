package co.unicauca.degreework.hexagonal.infra.config;

import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;

@Converter(autoApply = true)
public class TituloConverter implements AttributeConverter<Titulo, String> {
    
    @Override
    public String convertToDatabaseColumn(Titulo titulo) {
        return Optional.ofNullable(titulo)
                .map(Titulo::getValor)
                .orElse(null);
    }

    @Override
    public Titulo convertToEntityAttribute(String valor) {
        return Optional.ofNullable(valor)
                .map(Titulo::createTitulo)
                .orElse(null);
    }
}