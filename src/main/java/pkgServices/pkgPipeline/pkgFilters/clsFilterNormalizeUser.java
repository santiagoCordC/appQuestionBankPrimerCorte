package pkgServices.pkgPipeline.pkgFilters;

import java.util.Locale;
import pkgDomain.pkgEntities.clsUserData;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * Primer filtro de la tubería de registro (transformación): quita espacios
 * sobrantes y pasa el usuario y el correo a minúsculas.
 */
public class clsFilterNormalizeUser implements IFilter<clsUserData> {

    @Override
    public void opExecute(clsPipeData<clsUserData> prmData) {
        clsUserData varUser = prmData.opGetValue();
        varUser.opSetFullName(varUser.opGetFullName().replaceAll("\\s+", " ").trim());
        varUser.opSetNickName(varUser.opGetNickName().trim().toLowerCase(Locale.ROOT));
        varUser.opSetEmail(varUser.opGetEmail().trim().toLowerCase(Locale.ROOT));
    }
}
