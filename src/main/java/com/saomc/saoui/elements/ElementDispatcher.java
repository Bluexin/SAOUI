package com.saomc.saoui.elements;

import com.google.common.collect.LinkedHashMultimap;
import com.saomc.saoui.api.screens.GuiSelection;
import com.saomc.saoui.api.screens.ParentElement;
import com.saomc.saoui.util.LogCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Sends the elements to the correct classes to be rendered
 * <p>
 * Created by Tencao on 30/07/2016.
 */
public class ElementDispatcher {

    private static final List<ElementController> menuElements = new ArrayList<>();
    private static ElementDispatcher ref;

    private ElementDispatcher() {
        // nill
    }

    /**
     * Should only ever be one instance to prevent duplication
     *
     * @return default element dispatcher
     */
    @SideOnly(Side.CLIENT)
    public static synchronized ElementDispatcher getInstance() {
        if (ref == null)
            // Only return one instance
            ref = new ElementDispatcher();
        return ref;
    }

    public static void close() {
        menuElements.forEach(ElementController::close);
        menuElements.clear();
    }

    public static void check() {
        menuElements.stream().filter(controller -> controller.elements.isEmpty()).forEach(controller -> menuElements.remove(controller));
    }

    @Getter
    public static boolean isEmpty() {
        return menuElements.isEmpty();
    }

    @Setter
    public static void clear() {
        menuElements.clear();
    }

    @Getter
    public static List<ElementController> getElements() {
        return menuElements;
    }

    public void dispatch(ParentElement parent, GuiSelection gui) {
        //FIXME - Optimize all of this
        //Elements need to be sent in groups to the ElementController, not together
        //Groups are done via parent category, as seen in the ElementBuilder MultiMap
        if (!menuElements.isEmpty()){
            LogCore.logWarn("Gui - " + gui + " called ElementDispatcher when list isn't empty \n" +
                    "Either list wasn't cleaned or it was called early\n" +
                    "List will now be cleaned");
            menuElements.clear();
        }
        if (menuElements.isEmpty()) {

            LinkedHashMultimap<String, Element> elementSort = LinkedHashMultimap.create();
            for (Element entry : ElementProvider.instance().getBuilder().getforGui(gui)) {
                elementSort.put(entry.getParent(), entry);
            }
            for (Map.Entry<String, Collection<Element>> entry : elementSort.asMap().entrySet())
                if (!entry.getKey().equals("null")) {
                    ElementController list = new ElementController(parent);
                    for (Element element : entry.getValue()) {
                        list.elements.add(element);
                        element.setParentElement(parent);
                    }
                    menuElements.add(list);
                }
        }
        LogCore.logDebug("Dispatched");
    }
}
