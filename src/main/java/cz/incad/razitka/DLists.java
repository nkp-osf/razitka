package cz.incad.razitka;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.client.shared.data.ContainerNodeDTO;
import org.aplikator.client.shared.data.RecordDTO;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.razitka.server.Structure;

public class DLists extends Entity {
    public Property<String> classType;
    public Property<String> value;
    public Property<String> cz;
    public Property<String> en;
    public Property<String> de;
    public Property<String> fr;
    public Property<Boolean> use;
    public Property<Integer> poradi;
    public Property<String> poznamka;
    
    public DLists() {
        super("DLists","DLists", "DLists_ID");
        initFields();
        this.setPersistersTriggers(new DlistTriggers());
    }
    
    class DlistTriggers extends PersisterTriggers.Default {        
        @Override
        public void afterCommit(ContainerNodeDTO node, Context ctx) {
            super.afterCommit(node, ctx);
            String listName = classType.getValue(node.getMerged());
            if (listName==null) {
                return;
            }
            DListProvider listProvider = (DListProvider) Structure.listProviders.get(DListsType.valueOf(listName));
            if (listProvider!=null) {
                listProvider.refreshListValues(ctx);
            }
        }
        
        @Override
        public void onLoad(RecordDTO record, Context ctx) {
            record.setPreview(cz.getValue(record)+" - "+value.getValue(record));
        }
    }
    
    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(value).addProperty(cz);
        Form form = new Form(false);
        form.setLayout(column(
                row(value, cz),
                row(en, de, fr),
                row(use, poradi),
                row(new TextArea(poznamka).setRows(3))
                ));
        retval.setForm(form);
        return retval;
    }
    
    public enum DListsType {
        druh, obecne
    }

    public View druh() {
        return inheritanceView(this.view(), classType, DListsType.druh);
    }

    public View obecne() {
        return inheritanceView(this.view(), classType, DListsType.obecne);
    }


    public void initFields() {
        classType = stringProperty("classType");
        value = stringProperty("value");
        cz = stringProperty("cz");
        en = stringProperty("en");
        de = stringProperty("de");
        fr = stringProperty("fr");
        use = booleanProperty("use");
        poradi = integerProperty("poradi");
        poznamka = stringProperty("poznamka");
    }
}
