package pkgServices.pkgPipeline.pkgFilters;

import pkgDomain.pkgEntities.clsUserData;
import pkgServices.pkgPipeline.IFilter;
import pkgServices.pkgPipeline.clsPipeData;

/**
 * La contraseña y su confirmación deben ser iguales.
 */
public class clsFilterPasswordsMatch implements IFilter<clsUserData> {

    @Override
    public void opExecute(clsPipeData<clsUserData> prmData) {
        clsUserData varUser = prmData.opGetValue();
        if (!varUser.opGetPassword().equals(varUser.opGetPasswordConfirm())) {
            prmData.opAddError("La contraseña y su confirmación no coinciden.");
        }
    }
}
