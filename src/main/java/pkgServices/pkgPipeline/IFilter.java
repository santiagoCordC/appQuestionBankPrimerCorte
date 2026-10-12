package pkgServices.pkgPipeline;

/**
 * Un filtro del estilo Tuberías y Filtros: recibe lo que viene por la
 * tubería, lo procesa (lo limpia o lo revisa y anota errores) y lo deja
 * listo para el siguiente filtro. Un filtro no conoce a los demás.
 *
 * @param <T> tipo del dato que procesa.
 */
public interface IFilter<T> {

    void opExecute(clsPipeData<T> prmData);
}
