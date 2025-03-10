/**
 * 
 */
package at.porscheinformatik.idp.openidconnect.convert;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Daniel Furtlehner
 * @param <T> type of object to convert
 */
public abstract class AbstractCollectionConverter<T> implements Converter<Object, Collection<T>>
{
    public static final ParameterizedTypeReference<Map<String, Object>> MAP =
        new ParameterizedTypeReference<Map<String, Object>>()
        {
            // Nothing to do here.
        };

    @Override
    public Collection<T> convert(Object source)
    {
        if (source == null)
        {
            return null;
        }

        Collection<Map<String, Object>> sourceCollection = ConverterUtils.cast(source, MAP);

        return sourceCollection //
            .stream()
            .map(this::doConvertEntry)
            .collect(Collectors.toList());
    }

    protected abstract T doConvertEntry(Map<String, Object> entry);

}
