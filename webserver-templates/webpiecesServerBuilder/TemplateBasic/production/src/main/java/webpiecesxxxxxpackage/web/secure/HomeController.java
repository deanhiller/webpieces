package webpiecesxxxxxpackage.web.secure;

import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

import javax.inject.Singleton;

@Singleton
public class HomeController {

    public Render loginHome() {
        return Actions.renderThis();
    }

}
