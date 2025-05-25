module com.leave.engine { 

    //@What: requires, gets a requirement from another module
    // this is a "requirement". if module not there, it wont compile 
    // this is a requrement for this to compile, but i donno not quite sure java is weird 
    requires javafx.controls; 
    requires javafx.fxml;
    requires javafx.graphics; 
    requires javafx.base;
    requires javafx.media;
    requires java.desktop;
    

    requires com.fasterxml.jackson.core;    
    requires com.fasterxml.jackson.databind; 
    
    
    // @What: exports, makes a package visible to other modules
    // in this case it makes engine visible to javafx.fxml meaning 
    opens com.leave.engine to javafx.fxml;
    opens com.leave.engine.utils to javafx.fxml;

    // allows reflective access for specific purposes. like it gives it all access even private
    exports com.leave.engine;
    exports com.leave.engine.utils;

}


// @notes
/**
 * `module-info.java` defines a module's contract with the outside world (other modules):
 * 1.  `requires`: What external code (from other modules) this module needs to function.
 * 2.  `exports`: What public parts of this module's own code are made available for other modules to use directly.
 * 3.  `opens`: Which specific internal parts of this module are made available for deep reflective access by specific other modules (often frameworks).
 *
 * Encapsulation in JPMS:
 * - By default, everything inside a module is strongly encapsulated.
 * - `exports` selectively breaks encapsulation for public types in specified packages for normal code usage.
 * - `opens` selectively breaks encapsulation for all members (including private) in specified packages for reflective access by trusted frameworks.
 * - Without these directives, code in other modules cannot see or use code from this module (for `exports`)
 *   or reflectively access its internals (for `opens`).
 *
 * @remember:
 *  - `exports` is for *other code to use your code* (compilation, imports, method calls).
 *  - `opens` is for *other frameworks to inspect and manipulate your code* (reflection, often on non-public members).
 * 
 * @ I like this anallogy (LOL):
 *  Think of it with an analogy of a house:
 * Your Module (com.leave.engine) is a House.
 * requires javafx.fxml;: Your house needs tools (a special screwdriver, a special key duplicator)
 * from the javafx.fxml "store."
 * Packages inside your module (e.g., com.leave.engine, com.leave.engine.utils) are Rooms in your house.
 * exports com.leave.engine;: You put a sign on the front door: "Visitors (other modules that require me) can come into the Living Room (com.leave.engine package) and use the public items there (public classes)." They cannot go into the Bedroom unless you also export the Bedroom package.
 * opens com.leave.engine to javafx.fxml;: You give a special key for the Living Room (com.leave.engine package) only to the javafx.fxml "inspector." This inspector can now open all drawers, look under the bed, check the wiring inside the walls of the Living Room (i.e., access private members via reflection). Regular visitors (from the exports rule) cannot do this; they can only use what's publicly displayed.
 */