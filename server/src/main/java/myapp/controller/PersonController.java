package myapp.controller;

import java.util.Collections;
import java.util.List;

import org.opendolphin.core.Dolphin;
import org.opendolphin.core.server.DTO;
import org.opendolphin.core.server.ServerPresentationModel;
import org.opendolphin.core.server.Slot;
import org.opendolphin.core.server.comm.ActionRegistry;

import myapp.controller.util.Controller;
import myapp.presentationmodel.PMDescription;
import myapp.presentationmodel.SpecialPMMixin;
import myapp.presentationmodel.person.Person;
import myapp.presentationmodel.person.PersonCommands;
import myapp.service.SomeService;

/**
 * This is an example for an application specific controller.
 * <p>
 * Controllers may have many actions that serve a common purpose.
 * <p>
 * Todo: Replace this with your Controller
 */
class PersonController extends Controller implements SpecialPMMixin {

    private final SomeService service;
    private       Person      personProxy;

    PersonController(SomeService service) {
        this.service = service;
    }

    @Override
    public void registerIn(ActionRegistry actionRegistry) {
        super.registerIn(actionRegistry);
        actionRegistry.register(PersonCommands.LOAD_SOME_PERSON, (command, response) -> loadPerson());
        actionRegistry.register(PersonCommands.SAVE            , (command, response) -> save());
        actionRegistry.register(PersonCommands.RESET           , (command, response) -> reset(PMDescription.PERSON, Collections.emptyList()));
    }

    @Override
    protected void initializeBasePMs() {
        List<Slot> proxySlots = createProxySlots(PMDescription.PERSON);

        ServerPresentationModel pm = createPM(PERSON_PROXY_PM_ID,
                                              "Proxy:" + PMDescription.PERSON.getName(),
                                              new DTO(proxySlots));
        personProxy = new Person(pm);
    }

    @Override
    protected void setDefaultValues() {
        personProxy.name.setMandatory(true);
        personProxy.age.setValue(10);
        personProxy.isAdult.setValue(false);
    }

    @Override
    protected void setupValueChangedListener() {
        super.setupValueChangedListener();
        getPresentationState().language.valueProperty().addListener((observable, oldValue, newValue) -> {
            translate(personProxy, newValue);
        });
    }

    ServerPresentationModel loadPerson() {
        DTO dto = service.loadSomeEntity();
        ServerPresentationModel pm = createPM(PMDescription.PERSON, dto);

        personProxy.getPresentationModel().syncWith(pm);

        return pm;
    }

    void save() {
        List<DTO> dtos = dirtyDTOs(PMDescription.PERSON, Collections.emptyList());
        service.save(dtos);
        rebase(PMDescription.PERSON, Collections.emptyList());
    }

    @Override
    public Dolphin getDolphin() {
        return getServerDolphin();
    }
}