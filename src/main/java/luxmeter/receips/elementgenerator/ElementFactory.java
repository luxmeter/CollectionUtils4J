package luxmeter.receips.elementgenerator;

import java.util.Set;

public interface ElementFactory<T> {
    T createConcreteElement(Set<ElementAbstraction> allGeneratedElements, ElementAbstraction toGenerateFrom);
}
