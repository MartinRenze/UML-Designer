package adeditor;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.CallAction;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.InvocationAction;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.UMLFactory;

public class ADEditorService {
	
	private FunctionBehavior function = null;
	private String cpp = null;
	private String inc = null;
	private String lib = null;
	
	private void init(FunctionBehavior context) {
		if (!context.equals(function)) {
			function = context;
			EList<String> bodies = context.getBodies();
			
			cpp = "//CPP";
			inc = "//INCLUDE";
			lib = "//LIBRARY";
			
			if (!bodies.isEmpty()) {
				if (bodies.size() > 0) cpp = bodies.get(0);
				if (bodies.size() > 1) inc = bodies.get(1);
				if (bodies.size() > 2) lib = bodies.get(2);
			}
		}
	}
	
	public String getBody(FunctionBehavior context, String lang) {
		init(context);
		
		if (lang.equalsIgnoreCase("CPP")) {
			return cpp;
		} else if (lang.equalsIgnoreCase("INCLUDE")) {
			return inc;
		} else if (lang.equalsIgnoreCase("LIBRARY")) {
			return lib;
		}
		return "";
	}
	
	public Activity setBody(FunctionBehavior context, String body, String lang) {
		init(context);
		
		if (lang.equalsIgnoreCase("CPP")) {
			cpp = body;
		} else if (lang.equalsIgnoreCase("INCLUDE")) {
			inc = body;
		} else if (lang.equalsIgnoreCase("LIBRARY")) {
			lib = body;
		}
		saveBody(context);
		return null;
	}
	
	private void saveBody(FunctionBehavior context) {
		EList<String> bodies = context.getBodies();
		bodies.clear();
		bodies.add(cpp);
		bodies.add(inc);
		bodies.add(lib);
	}
	
	public Activity setLanguage(FunctionBehavior context, String lang, boolean set) {
		EList<String> langs = context.getLanguages();
		if (!set) { 
			langs.remove(lang); 
		} else { 
			if (lang.equalsIgnoreCase("CPP")) { 
				langs.add(0, lang);
			} else if (lang.equalsIgnoreCase("INCLUDE")) {
				langs.add(langs.indexOf("CPP") + 1, lang);
			} else if (lang.equalsIgnoreCase("LIBRARY")) {
				langs.add(lang);
			}
		}
		return null;
	}

	public boolean hasLanguage(FunctionBehavior context, String lang) {
		for (String s : context.getLanguages()) {
			if (s != null && lang.contains(s)) return true;
		}
		return false;
	}
	
	public OutputPin createCustomOutputPin(Action context, Parameter parameter) {
        final OutputPin pin = UMLFactory.eINSTANCE.createOutputPin();

        if (context instanceof CallAction) {
            ((CallAction) context).getResults().add(pin);
        } else if (context instanceof OpaqueAction) {
            ((OpaqueAction) context).getOutputValues().add(pin);
        } else {
            throw new UnsupportedOperationException("Can't create InputPin for context of type: " //$NON-NLS-1$
                    + context.eClass().getName());
        }

        setCustomPinAttributes(pin, parameter);
        return pin;
    }
	
    public InputPin createCustomInputPin(Action context, Parameter parameter) {
        final InputPin pin = UMLFactory.eINSTANCE.createInputPin();
        pin.setName("Input_" + context.getInputs().size()); //$NON-NLS-1$

        if (context instanceof InvocationAction) {
            ((InvocationAction) context).getArguments().add(pin);
        } else if (context instanceof OpaqueAction) {
            ((OpaqueAction) context).getInputValues().add(pin);
        } else {
            throw new UnsupportedOperationException("Can't create InputPin for context of type: " //$NON-NLS-1$
                    + context.eClass().getName());
        }

        setCustomPinAttributes(pin, parameter);
        return pin;
    }
	
    public void setCustomPinAttributes(Pin pin, Parameter parameter) {
        pin.setName(parameter.getName());
        pin.setIsOrdered(parameter.isOrdered());
        pin.setType(parameter.getType());
        pin.setLowerValue(parameter.getLowerValue());
        pin.setUpperValue(parameter.getUpperValue());
    }
}
