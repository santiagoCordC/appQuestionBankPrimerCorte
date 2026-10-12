package pkgTestDoubles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import pkgDomain.pkgEntities.clsQuestion;
import pkgDomain.pkgEntities.clsReviewAssignment;
import pkgDomain.pkgEntities.clsUser;
import pkgDomain.pkgEntities.enRole;
import pkgDomain.pkgRepository.IAssignmentRepository;
import pkgDomain.pkgRepository.IQuestionRepository;
import pkgDomain.pkgRepository.IUserRepository;
import pkgDomain.pkgLifecycle.clsQuestionState;

/**
 * Repositorios en memoria para probar los controladores sin base de datos.
 * Gracias a las interfaces de repositorio (inversión de dependencias) los
 * controladores no notan la diferencia.
 */
public final class clsMemoryRepositories {

    private clsMemoryRepositories() {
    }

    public static class Users implements IUserRepository {

        public final List<clsUser> attItems = new ArrayList<>();

        @Override
        public boolean opSave(clsUser prmUser) {
            return attItems.add(prmUser);
        }

        @Override
        public clsUser opFindById(String prmId) {
            return attItems.stream().filter(varU -> varU.opGetId().equals(prmId)).findFirst().orElse(null);
        }

        @Override
        public clsUser opFindByNickName(String prmNickName) {
            return attItems.stream().filter(varU -> varU.opGetNickName().equalsIgnoreCase(prmNickName.trim()))
                    .findFirst().orElse(null);
        }

        @Override
        public List<clsUser> opFindByRole(enRole prmRole) {
            return attItems.stream().filter(varU -> varU.opHasRole(prmRole)).collect(Collectors.toList());
        }

        @Override
        public int opCount() {
            return attItems.size();
        }
    }

    public static class Questions implements IQuestionRepository {

        public final List<clsQuestion> attItems = new ArrayList<>();
        /** Último estado guardado de cada pregunta, para comprobar que el controlador sí persiste. */
        public final List<clsQuestionState> attSavedStates = new ArrayList<>();
        public boolean attFail;

        @Override
        public boolean opSave(clsQuestion prmQuestion) {
            if (attFail) {
                return false;
            }
            attSavedStates.add(prmQuestion.opGetState());
            return attItems.add(prmQuestion);
        }

        @Override
        public boolean opUpdate(clsQuestion prmQuestion) {
            if (attFail) {
                return false;
            }
            attSavedStates.add(prmQuestion.opGetState());
            return attItems.contains(prmQuestion);
        }

        @Override
        public clsQuestion opFindById(String prmId) {
            return attItems.stream().filter(varQ -> varQ.opGetId().equals(prmId)).findFirst().orElse(null);
        }

        @Override
        public List<clsQuestion> opFindAll() {
            return new ArrayList<>(attItems);
        }
    }

    public static class Assignments implements IAssignmentRepository {

        public final List<clsReviewAssignment> attItems = new ArrayList<>();

        @Override
        public boolean opSave(clsReviewAssignment prmAssignment) {
            return attItems.add(prmAssignment);
        }

        @Override
        public List<clsReviewAssignment> opFindByQuestion(String prmQuestionId) {
            return attItems.stream().filter(varA -> varA.opGetQuestion().opGetId().equals(prmQuestionId))
                    .collect(Collectors.toList());
        }
    }
}
