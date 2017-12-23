package navimateforbusiness

import java.security.Principal

/**
 * Created by Siddharth on 21-12-2017.
 */
class StompPrincipal implements Principal {
    String name

    StompPrincipal(String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }
}
