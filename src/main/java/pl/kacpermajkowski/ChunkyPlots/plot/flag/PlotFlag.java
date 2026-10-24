package pl.kacpermajkowski.ChunkyPlots.plot.flag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PlotFlag {
    PVP("pvp", "PVP", "zezwala na walkę między graczami na działce", false),
    EXPLOSIONS("explosions", "Eksplozje", "zezwala na niszczenie bloków przez eksplozje (TNT, creepery)", false, "explosion", "tnt"),
    FIRE_SPREAD("fire-spread", "Rozprzestrzenianie ognia", "zezwala na rozprzestrzenianie się ognia i spalanie bloków", false, "fire_spread", "fire"),
    BLOCK_BREAK("block-break", "Niszczenie bloków", "zezwala obcym graczom na niszczenie bloków", false, "block_break", "break"),
    BLOCK_PLACE("block-place", "Stawianie bloków", "zezwala obcym graczom na stawianie bloków", false, "block_place", "place"),
    INTERACT("interact", "Interakcja z blokami", "zezwala obcym na otwieranie drzwi, skrzyń, używanie przycisków i dźwigni", false, "use"),
    ANIMAL_DAMAGE("animal-damage", "Atakowanie zwierząt", "zezwala obcym graczom na atakowanie zwierząt i mobów", false, "animal_damage", "mob-damage", "pve"),
    ANIMAL_INTERACT("animal-interact", "Interakcja ze stworzeniami", "zezwala obcym na interakcję ze zwierzętami i wieśniakami", false, "animal_interact", "entity-interact"),
    ITEM_FRAME("item-frame", "Ramki na przedmioty", "zezwala obcym na niszczenie i obracanie ramek oraz obrazów", false, "item_frame", "hanging"),
    VEHICLE_ENTER("vehicle-enter", "Wsiadanie do pojazdów", "zezwala obcym na wsiadanie do wagoników i łódek", false, "vehicle_enter"),
    VEHICLE_DAMAGE("vehicle-damage", "Niszczenie pojazdów", "zezwala obcym na niszczenie pojazdów", false, "vehicle_damage"),
    LIQUID_FLOW("liquid-flow", "Wpływanie płynów", "zezwala na wpływanie wody i lawy z zewnątrz", false, "liquid_flow", "water-flow", "lava-flow"),
    PISTON("piston", "Działanie tłoków", "zezwala zewnętrznym tłokom na przesuwanie bloków na działce", false, "pistons"),
    REDSTONE("redstone", "Sygnał redstone", "zezwala na przechodzenie sygnału redstone z zewnątrz", false, "redstone-signal"),
    STRUCTURE_GROW("structure-grow", "Wrastanie struktur", "zezwala drzewom i strukturom na wrastanie z zewnątrz", false, "structure_grow", "trees"),
    POTIONS("potions", "Działanie mikstur", "zezwala obcym na rzucanie mikstur wpływających na działkę", false, "potion-splash"),
    LIGHTNING("lightning", "Pioruny", "zezwala na uderzenia piorunów i wywoływane przez nie szkody", false),
    ANIMAL_LEASH("animal-leash", "Używanie smyczy", "zezwala obcym na przypinanie i odpinanie smyczy", false, "animal_leash", "leash"),
    ANIMAL_SHEAR("animal-shear", "Strzyżenie zwierząt", "zezwala obcym na strzyżenie owiec", false, "animal_shear", "shear"),
    ANIMAL_TEMPT("animal-tempt", "Nęcenie zwierząt", "zezwala obcym na wyprowadzanie zwierząt za pomocą jedzenia", false, "animal_tempt", "tempt"),
    LECTERN("lectern", "Pulpity z książkami", "zezwala obcym na wyjmowanie książek z pulpitów", false),
    ENTRY("entry", "Ochrona wejścia", "blokuje wstęp graczom znajdującym się na czarnej liście", true, "deny"),
    KNOCKBACK("knockback", "Odrzut stworzeń", "zezwala na odrzucanie stworzeń przez granicę działki", false),
    DISPENSER("dispenser", "Dozowniki", "zezwala zewnętrznym dozownikom na wyrzucanie przedmiotów na działkę", false, "dispensers"),
    HOPPER("hopper", "Leje", "zezwala zewnętrznym lejom na wyciąganie przedmiotów z działki", false, "hoppers"),
    SCULK_SHRIEKER("sculk-shrieker", "Sculk Shrieker", "zezwala obcym na aktywowanie shriekerów i przywoływanie Wardena", false, "shrieker");

    private final String name;
    private final String displayName;
    private final String description;
    private final boolean defaultValue;
    private final List<String> aliases;

    PlotFlag(String name, String displayName, String description, boolean defaultValue, String... aliases) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.aliases = aliases != null ? Arrays.asList(aliases) : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static PlotFlag getByName(String query) {
        if (query == null) return null;
        String normalized = query.trim().toLowerCase().replace('_', '-');
        for (PlotFlag flag : values()) {
            if (flag.name.equalsIgnoreCase(normalized)) {
                return flag;
            }
            for (String alias : flag.aliases) {
                if (alias.toLowerCase().replace('_', '-').equalsIgnoreCase(normalized)) {
                    return flag;
                }
            }
        }
        return null;
    }
}
