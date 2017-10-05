/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class ManualLexicon {

    private static HashMap<String, Set<String>> lexiconPropertiesEN;
    private static HashMap<String, Set<String>> lexiconClassesEN;
    private static HashMap<String, Set<String>> lexiconRestrictionClassesEN;
    private static HashMap<String, Set<String>> lexiconResourcesEN;

    private static HashMap<String, Set<String>> lexiconPropertiesDE;
    private static HashMap<String, Set<String>> lexiconClassesDE;
    private static HashMap<String, Set<String>> lexiconRestrictionClassesDE;
    private static HashMap<String, Set<String>> lexiconResourcesDE;

    private static HashMap<String, Set<String>> lexiconPropertiesES;
    private static HashMap<String, Set<String>> lexiconClassesES;
    private static HashMap<String, Set<String>> lexiconRestrictionClassesES;
    private static HashMap<String, Set<String>> lexiconResourcesES;

    public static boolean useManualLexicon = false;

    private static boolean loaded = false;

    public static void useManualLexicon(boolean b) {
        useManualLexicon = b;
        if (b) {
            load();
        }
    }

    public static void load() {
        lexiconPropertiesEN = new HashMap<>();
        lexiconClassesEN = new HashMap<>();
        lexiconRestrictionClassesEN = new HashMap<>();
        lexiconResourcesEN = new HashMap<>();

        lexiconPropertiesDE = new HashMap<>();
        lexiconClassesDE = new HashMap<>();
        lexiconRestrictionClassesDE = new HashMap<>();
        lexiconResourcesDE = new HashMap<>();

        lexiconPropertiesES = new HashMap<>();
        lexiconClassesES = new HashMap<>();
        lexiconRestrictionClassesES = new HashMap<>();
        lexiconResourcesES = new HashMap<>();

        if (useManualLexicon) {

            Set<String> content = FileFactory.readFile("Manual_Lexicon.txt");

            for (String c : content) {
                String[] data = c.split("\t");

                String surfaceForm = data[1];
                String uri = data[2];
                String language = data[3];
                String dataset = data[4];
                
                if (uri.split(",").length > 1) {
                    continue;
                }

                switch (language) {
                    case "EN":
                        //classes, restriction classes
                        if (uri.contains("###")) {
                            if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                addLexicon(surfaceForm, uri, lexiconClassesEN);
                            } else {
                                addLexicon(surfaceForm, uri, lexiconRestrictionClassesEN);
                            }
                        } else {
                            if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                addLexicon(surfaceForm, uri, lexiconPropertiesEN);
                            } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                addLexicon(surfaceForm, uri, lexiconResourcesEN);
                            }
                        }

                        break;
                    case "DE":
                        //classes, restriction classes
                        if (uri.contains("###")) {
                            if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                addLexicon(surfaceForm, uri, lexiconClassesDE);
                            } else {
                                addLexicon(surfaceForm, uri, lexiconRestrictionClassesDE);
                            }
                        } else {
                            if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                addLexicon(surfaceForm, uri, lexiconPropertiesDE);
                            } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                addLexicon(surfaceForm, uri, lexiconResourcesDE);
                            }
                        }

                        break;
                    case "ES":
                        //classes, restriction classes
                        if (uri.contains("###")) {
                            if (uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#type###")) {
                                addLexicon(surfaceForm, uri, lexiconClassesES);
                            } else {
                                addLexicon(surfaceForm, uri, lexiconRestrictionClassesES);
                            }
                        } else {
                            if (uri.startsWith("http://dbpedia.org/ontology/") || uri.startsWith("http://dbpedia.org/property/")) {
                                addLexicon(surfaceForm, uri, lexiconPropertiesES);
                            } else if (uri.startsWith("http://dbpedia.org/resource/")) {
                                addLexicon(surfaceForm, uri, lexiconResourcesES);
                            }
                        }

                        break;

                }
            }
        }

        loaded = true;

    }

    private static void loadTrainLexiconDE() {
        addLexicon("erfunden", "http://dbpedia.org/ontology/creator", lexiconPropertiesDE);
    }

    private static void loadTrainLexiconEN() {

        //QALD-6 Train
        //resources
        addLexicon("john lennon", "http://dbpedia.org/resource/Death_of_John_Lennon", lexiconResourcesEN);
        addLexicon("gmt", "http://dbpedia.org/resource/GMT_Games", lexiconResourcesEN);
        addLexicon("lighthouse in colombo", "http://dbpedia.org/resource/Colombo_Lighthouse", lexiconResourcesEN);
        addLexicon("mn", "'MN'@en", lexiconResourcesEN);
        addLexicon("baldwin", "'Baldwin'@en", lexiconResourcesEN);
        addLexicon("rodzilla", "'Rodzilla'@en", lexiconResourcesEN);
        addLexicon("iycm", "'IYCM'@en", lexiconResourcesEN);
        addLexicon("president of the united states", "'President of the United States'", lexiconResourcesEN);
        addLexicon("ireland", "\"Ireland\"@en", lexiconResourcesEN);
        addLexicon("japanese musical instruments", "http://dbpedia.org/class/yago/JapaneseMusicalInstruments", lexiconResourcesEN);
        addLexicon("eating disorders", "http://dbpedia.org/class/yago/EatingDisorders", lexiconResourcesEN);
        addLexicon("vatican television", "http://dbpedia.org/resource/Vatican_Television_Center", lexiconResourcesEN);
        addLexicon("U.S. president Lincoln", "http://dbpedia.org/resource/Abraham_Lincoln", lexiconResourcesEN);
        addLexicon("battle chess", "'Battle Chess'@en", lexiconResourcesEN);
//        addLexicon("juan carlos i", "http://dbpedia.org/resource/Juan_Carlos_I_of_Spain", lexiconResourcesEN);

        //properties
        addLexicon("play", "http://dbpedia.org/ontology/league", lexiconPropertiesEN);
        addLexicon("start", "http://dbpedia.org/ontology/routeStart", lexiconPropertiesEN);
        addLexicon("country", "http://dbpedia.org/ontology/foundationPlace", lexiconPropertiesEN);
        addLexicon("star", "http://dbpedia.org/ontology/starring", lexiconPropertiesEN);
        addLexicon("play", "http://dbpedia.org/ontology/starring", lexiconPropertiesEN);
        addLexicon("player", "http://dbpedia.org/ontology/team", lexiconPropertiesEN);
        addLexicon("produced", "http://dbpedia.org/ontology/assembly", lexiconPropertiesEN);
        addLexicon("produced", "http://dbpedia.org/ontology/assembly", lexiconPropertiesEN);
        addLexicon("influence", "http://dbpedia.org/ontology/influenced", lexiconPropertiesEN);
        addLexicon("completed", "http://dbpedia.org/ontology/completionDate", lexiconPropertiesEN);
        addLexicon("official color", "http://dbpedia.org/ontology/officialSchoolColour", lexiconPropertiesEN);

        //prepositions
        addLexicon("with", "http://dbpedia.org/ontology/starring", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/location", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/locatedInArea", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/league", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/country", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/isPartOf", lexiconPropertiesEN);
        addLexicon("from", "http://dbpedia.org/ontology/birthPlace", lexiconPropertiesEN);
        addLexicon("a", "http://dbpedia.org/ontology/profession", lexiconPropertiesEN);
        addLexicon("by", "http://dbpedia.org/ontology/author", lexiconPropertiesEN);
        addLexicon("from", "http://dbpedia.org/ontology/artist", lexiconPropertiesEN);

        addLexicon("pages", "http://dbpedia.org/ontology/numberOfPages", lexiconPropertiesEN);
        addLexicon("artistic movement", "http://dbpedia.org/ontology/movement", lexiconPropertiesEN);
        addLexicon("tall", "http://dbpedia.org/ontology/height", lexiconPropertiesEN);
        addLexicon("high", "http://dbpedia.org/ontology/height", lexiconPropertiesEN);
        addLexicon("high", "http://dbpedia.org/ontology/elevation", lexiconPropertiesEN);
        addLexicon("type", "http://dbpedia.org/ontology/class", lexiconPropertiesEN);
        addLexicon("employees", "http://dbpedia.org/ontology/numberOfEmployees", lexiconPropertiesEN);
        addLexicon("total population", "http://dbpedia.org/ontology/populationTotal", lexiconPropertiesEN);
        addLexicon("military conflicts", "http://dbpedia.org/ontology/battle", lexiconPropertiesEN);
        addLexicon("belong", "http://dbpedia.org/ontology/country", lexiconPropertiesEN);
        addLexicon("grow", "http://dbpedia.org/ontology/growingGrape", lexiconPropertiesEN);
        addLexicon("official color", "http://dbpedia.org/ontology/officialSchoolColour", lexiconPropertiesEN);
        addLexicon("timezone", "http://dbpedia.org/ontology/timeZone", lexiconPropertiesEN);
        addLexicon("timezone", "http://dbpedia.org/ontology/timezone", lexiconPropertiesEN);
        addLexicon("stand", "http://dbpedia.org/property/name", lexiconPropertiesEN);
        addLexicon("stand", "http://dbpedia.org/ontology/abbreviation", lexiconPropertiesEN);
        addLexicon("involved", "http://dbpedia.org/ontology/battle", lexiconPropertiesEN);
        addLexicon("killed", "http://dbpedia.org/property/conviction", lexiconPropertiesEN);
//        addLexicon("games", "http://dbpedia.org/ontology/publisher", lexiconPropertiesEN);
        addLexicon("region", "http://dbpedia.org/ontology/wineRegion", lexiconPropertiesEN);
        addLexicon("stores", "http://dbpedia.org/ontology/numberOfLocations", lexiconPropertiesEN);
        addLexicon("inhabitants", "http://dbpedia.org/ontology/populationTotal", lexiconPropertiesEN);
        addLexicon("mayor", "http://dbpedia.org/ontology/leader", lexiconPropertiesEN);
        addLexicon("professional", "http://dbpedia.org/ontology/occupation", lexiconPropertiesEN);
        addLexicon("connected", "http://dbpedia.org/property/country", lexiconPropertiesEN);
        addLexicon("killed", "http://dbpedia.org/property/conviction", lexiconPropertiesEN);
        addLexicon("flow through", "http://dbpedia.org/ontology/city", lexiconPropertiesEN);

        addLexicon("painted", "http://dbpedia.org/ontology/author", lexiconPropertiesEN);
        addLexicon("painter", "http://dbpedia.org/ontology/author", lexiconPropertiesEN);
        addLexicon("country", "http://dbpedia.org/ontology/nationality", lexiconPropertiesEN);
        addLexicon("actors", "http://dbpedia.org/ontology/starring", lexiconPropertiesEN);
        addLexicon("birthdays", "http://dbpedia.org/ontology/birthDate", lexiconPropertiesEN);
        addLexicon("flow through", "http://dbpedia.org/ontology/city", lexiconPropertiesEN);

        addLexicon("dwelt", "http://dbpedia.org/property/abode", lexiconPropertiesEN);
        addLexicon("time zone", "http://dbpedia.org/property/timezone", lexiconPropertiesEN);
        addLexicon("called", "http://dbpedia.org/property/shipNamesake", lexiconPropertiesEN);
        addLexicon("called", "http://dbpedia.org/property/nickname", lexiconPropertiesEN);
        addLexicon("called", "http://xmlns.com/foaf/0.1/surname", lexiconPropertiesEN);
        addLexicon("called", "http://www.w3.org/2000/01/rdf-schema#label", lexiconPropertiesEN);

        addLexicon("built", "http://dbpedia.org/property/beginningDate", lexiconPropertiesEN);
        addLexicon("border", "http://dbpedia.org/property/borderingstates", lexiconPropertiesEN);
        addLexicon("type", "http://dbpedia.org/property/design", lexiconPropertiesEN);
        addLexicon("abbreviation", "http://dbpedia.org/property/postalabbreviation", lexiconPropertiesEN);
        addLexicon("population density", "http://dbpedia.org/property/densityrank", lexiconPropertiesEN);
        addLexicon("first name", "http://xmlns.com/foaf/0.1/givenName", lexiconPropertiesEN);
        addLexicon("websites", "http://dbpedia.org/property/homepage", lexiconPropertiesEN);
        addLexicon("birth name", "http://dbpedia.org/property/birthName", lexiconPropertiesEN);
        addLexicon("governed", "http://dbpedia.org/ontology/leaderParty", lexiconPropertiesEN);
        addLexicon("span", "http://dbpedia.org/ontology/mainspan", lexiconPropertiesEN);
        addLexicon("run through", "http://dbpedia.org/property/country", lexiconPropertiesEN);
        addLexicon("moon", "http://dbpedia.org/property/satelliteOf", lexiconPropertiesEN);
        addLexicon("heavy", "http://dbpedia.org/ontology/mass", lexiconPropertiesEN);
        addLexicon("shot", "http://dbpedia.org/property/dateOfDeath", lexiconPropertiesEN);
        addLexicon("part", "http://dbpedia.org/property/alliance", lexiconPropertiesEN);
        addLexicon("members", "http://dbpedia.org/property/alliance", lexiconPropertiesEN);
        addLexicon("flew", "http://dbpedia.org/property/planet", lexiconPropertiesEN);
        addLexicon("cost", "http://dbpedia.org/ontology/budget", lexiconPropertiesEN);
        addLexicon("serve", "http://dbpedia.org/ontology/targetAirport", lexiconPropertiesEN);
        addLexicon("graduated", "http://dbpedia.org/ontology/almaMater", lexiconPropertiesEN);
        addLexicon("largest metropolitan area", "http://dbpedia.org/property/largestmetro", lexiconPropertiesEN);
        addLexicon("types", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", lexiconPropertiesEN);
        addLexicon("deep", "http://dbpedia.org/ontology/depth", lexiconPropertiesEN);
        addLexicon("astronauts", "http://dbpedia.org/ontology/mission", lexiconPropertiesEN);
        addLexicon("missions", "http://dbpedia.org/property/programme", lexiconPropertiesEN);
        addLexicon("cross", "http://dbpedia.org/ontology/crosses", lexiconPropertiesEN);
        addLexicon("students", "http://dbpedia.org/ontology/numberOfStudents", lexiconPropertiesEN);
        addLexicon("serves", "http://dbpedia.org/ontology/targetAirport", lexiconPropertiesEN);
        addLexicon("influenced", "http://dbpedia.org/ontology/influencedBy", lexiconPropertiesEN);
        addLexicon("die", "http://dbpedia.org/ontology/deathCause", lexiconPropertiesEN);
        addLexicon("launched", "http://dbpedia.org/ontology/launchSite", lexiconPropertiesEN);
        addLexicon("flow", "http://dbpedia.org/ontology/city", lexiconPropertiesEN);
        addLexicon("climb", "http://dbpedia.org/ontology/firstAscentPerson", lexiconPropertiesEN);

        //restriction classes
        addLexicon("democrat", "http://dbpedia.org/ontology/party###http://dbpedia.org/resource/Democratic_Party_(United_States)", lexiconRestrictionClassesEN);
        addLexicon("swedish", "http://dbpedia.org/ontology/birthPlace###http://dbpedia.org/resource/Sweden", lexiconRestrictionClassesEN);
        addLexicon("swedish", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Sweden", lexiconRestrictionClassesEN);
        addLexicon("dutch", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Netherlands", lexiconRestrictionClassesEN);
        addLexicon("oceanographers", "http://dbpedia.org/ontology/field###http://dbpedia.org/resource/Oceanography", lexiconRestrictionClassesEN);
        addLexicon("english gothic", "http://dbpedia.org/ontology/architecturalStyle###http://dbpedia.org/resource/English_Gothic_architecture", lexiconRestrictionClassesEN);
        addLexicon("nonprofit organizations", "http://dbpedia.org/ontology/type###http://dbpedia.org/resource/Nonprofit_organization", lexiconRestrictionClassesEN);
        addLexicon("danish", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Denmark", lexiconRestrictionClassesEN);
        addLexicon("canadian", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Canada", lexiconRestrictionClassesEN);
        addLexicon("greek", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Greece", lexiconRestrictionClassesEN);
        addLexicon("english", "http://dbpedia.org/ontology/birthPlace###http://dbpedia.org/resource/England", lexiconRestrictionClassesEN);
        addLexicon("grunge", "http://dbpedia.org/ontology/genre###http://dbpedia.org/resource/Grunge", lexiconRestrictionClassesEN);
        addLexicon("methodist", "http://dbpedia.org/ontology/religion###http://dbpedia.org/resource/Methodism", lexiconRestrictionClassesEN);
        addLexicon("australian", "http://dbpedia.org/ontology/hometown###http://dbpedia.org/resource/Australia", lexiconRestrictionClassesEN);
        addLexicon("australian", "http://dbpedia.org/ontology/locationCountry###http://dbpedia.org/resource/Australia", lexiconRestrictionClassesEN);
        addLexicon("spanish", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Spain", lexiconRestrictionClassesEN);

        addLexicon("metalcore", "http://dbpedia.org/ontology/genre###http://dbpedia.org/resource/Metalcore", lexiconRestrictionClassesEN);
        addLexicon("german", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Germany", lexiconRestrictionClassesEN);
        addLexicon("german", "http://dbpedia.org/ontology/birthPlace###http://dbpedia.org/resource/Germany", lexiconRestrictionClassesEN);
        addLexicon("jew", "http://dbpedia.org/property/ethnicity###'Jewish'@en", lexiconRestrictionClassesEN);
        addLexicon("politicians", "http://dbpedia.org/ontology/profession###http://dbpedia.org/resource/Politician", lexiconRestrictionClassesEN);
        addLexicon("chemist", "http://dbpedia.org/ontology/profession###http://dbpedia.org/resource/Chemist", lexiconRestrictionClassesEN);
        addLexicon("beer", "http://dbpedia.org/property/type###http://dbpedia.org/resource/Beer", lexiconRestrictionClassesEN);
        addLexicon("president of pakistan", "http://dbpedia.org/property/title###http://dbpedia.org/resource/President_of_Pakistan", lexiconRestrictionClassesEN);
        addLexicon("uk city", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/United_Kingdom", lexiconRestrictionClassesEN);
        addLexicon("pro-european", "http://dbpedia.org/ontology/ideology###http://dbpedia.org/resource/Pro-Europeanism", lexiconRestrictionClassesEN);
        addLexicon("non-profit organizations", "http://dbpedia.org/ontology/type###http://dbpedia.org/resource/Nonprofit_organization", lexiconRestrictionClassesEN);
        addLexicon("swiss", "http://dbpedia.org/ontology/locationCountry###http://dbpedia.org/resource/Switzerland", lexiconRestrictionClassesEN);

        //classes
        addLexicon("people", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://xmlns.com/foaf/0.1/foaf:Person", lexiconClassesEN);
        addLexicon("u.s. state", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/StatesOfTheUnitedStates", lexiconClassesEN);
        addLexicon("greek goddesses", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/GreekGoddesses", lexiconClassesEN);
        addLexicon("american inventions", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/AmericanInventions", lexiconClassesEN);
        addLexicon("films", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/Film", lexiconClassesEN);
        addLexicon("organizations", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/Company", lexiconClassesEN);
        addLexicon("capitals in europe", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/CapitalsInEurope", lexiconClassesEN);
        addLexicon("states of germany", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/StatesOfGermany", lexiconClassesEN);
        addLexicon("james bond movies", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type##http://dbpedia.org/class/yago/JamesBondFilms", lexiconClassesEN);
        addLexicon("city", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/City108524735", lexiconClassesEN);
        addLexicon("countries in africa", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/class/yago/AfricanCountries", lexiconClassesEN);
        addLexicon("organizations", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/Company", lexiconClassesEN);
        addLexicon("tv shows", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/TelevisionShow", lexiconClassesEN);
        addLexicon("parties", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/PoliticalParty", lexiconClassesEN);

        //QALD-6 Test Lexicon
        addLexicon("companies", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/Company", lexiconClassesEN);

    }

    private static void loadTestLexiconEN() {
        //QALD-6 Test Lexicon

        //classes
        addLexicon("companies", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type###http://dbpedia.org/ontology/Company", lexiconClassesEN);

        //properties
        addLexicon("discovered", "http://dbpedia.org/ontology/discoverer", lexiconPropertiesEN);
        addLexicon("expresses", "http://dbpedia.org/ontology/connotation", lexiconPropertiesEN);
        addLexicon("die", "http://dbpedia.org/property/deathCause", lexiconPropertiesEN);
        addLexicon("commence", "http://dbpedia.org/ontology/date", lexiconPropertiesEN);
        addLexicon("famous", "http://dbpedia.org/ontology/knownFor", lexiconPropertiesEN);
        addLexicon("live", "http://dbpedia.org/ontology/populationTotal", lexiconPropertiesEN);
        addLexicon("inspired", "http://dbpedia.org/ontology/influenced", lexiconPropertiesEN);
        addLexicon("form of government", "http://dbpedia.org/ontology/governmentType", lexiconPropertiesEN);
        addLexicon("government", "http://dbpedia.org/ontology/governmentType", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/ingredient", lexiconPropertiesEN);
        addLexicon("full name", "http://dbpedia.org/ontology/alias", lexiconPropertiesEN);
        addLexicon("kind of music", "http://dbpedia.org/ontology/genre", lexiconPropertiesEN);
        addLexicon("doctoral supervisor", "http://dbpedia.org/ontology/doctoralAdvisor", lexiconPropertiesEN);
        addLexicon("on", "http://dbpedia.org/property/crewMembers", lexiconPropertiesEN);
        addLexicon("calories", "http://dbpedia.org/ontology/approximateCalories", lexiconPropertiesEN);
        addLexicon("kind", "http://dbpedia.org/ontology/genre", lexiconPropertiesEN);
        addLexicon("in", "http://dbpedia.org/ontology/ingredient", lexiconPropertiesEN);
        addLexicon("end", "http://dbpedia.org/ontology/activeYearsEndDate", lexiconPropertiesEN);
        addLexicon("pay", "http://dbpedia.org/ontology/currency", lexiconPropertiesEN);
        addLexicon("home stadium", "http://dbpedia.org/ontology/ground", lexiconPropertiesEN);
        addLexicon("seats", "http://dbpedia.org/ontology/seatingCapacity", lexiconPropertiesEN);

        //resources
        addLexicon("kaurismäki", "http://dbpedia.org/resource/Aki_Kaurismäki", lexiconResourcesEN);
        addLexicon("Grand Prix at Cannes", "http://dbpedia.org/resource/Grand_Prix_(Cannes_Film_Festival)", lexiconResourcesEN);
        addLexicon("chocolate chip cookie", "http://dbpedia.org/resource/Chocolate_chip_cookie", lexiconResourcesEN);
        addLexicon("Sonny and Cher", "http://dbpedia.org/resource/Cher", lexiconResourcesEN);

        //restriction classes
        addLexicon("czech", "http://dbpedia.org/ontology/country###http://dbpedia.org/resource/Czech_Republic", lexiconRestrictionClassesEN);
        addLexicon("computer scientist", "http://dbpedia.org/ontology/field###http://dbpedia.org/resource/Computer_science", lexiconRestrictionClassesEN);
        addLexicon("canadian", "http://dbpedia.org/ontology/birthPlace###http://dbpedia.org/resource/Canada", lexiconRestrictionClassesEN);
        addLexicon("canadians", "http://dbpedia.org/ontology/birthPlace###http://dbpedia.org/resource/Canada", lexiconRestrictionClassesEN);
    }

    private static void addLexicon(String key, String value, HashMap<String, Set<String>> map) {

        key = key.toLowerCase().trim();
        value = value.trim();

        if (map.containsKey(key)) {
            Set<String> set = map.get(key);
            set.add(value);
            map.put(key, set);
        } else {
            Set<String> set = new HashSet<>();
            set.add(value);
            map.put(key, set);
        }
    }

    public static Set<String> getProperties(String term, Language lang) {

        term = term.toLowerCase();
        Set<String> result = new HashSet<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconPropertiesEN.containsKey(term)) {
                    result.addAll(lexiconPropertiesEN.get(term));
                }
                break;
            case DE:
                if (lexiconPropertiesDE.containsKey(term)) {
                    result.addAll(lexiconPropertiesDE.get(term));
                }
                break;
            case ES:
                if (lexiconPropertiesES.containsKey(term)) {
                    result.addAll(lexiconPropertiesES.get(term));
                }
                break;
        }
        return result;
    }

    public static Set<String> getRestrictionClasses(String term, Language lang) {

        term = term.toLowerCase();
        Set<String> result = new HashSet<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconRestrictionClassesEN.containsKey(term)) {
                    result.addAll(lexiconRestrictionClassesEN.get(term));
                }
                break;
            case DE:
                if (lexiconRestrictionClassesDE.containsKey(term)) {
                    result.addAll(lexiconRestrictionClassesDE.get(term));
                }
                break;
            case ES:
                if (lexiconRestrictionClassesES.containsKey(term)) {
                    result.addAll(lexiconRestrictionClassesES.get(term));
                }
                break;
        }
        return result;
    }

    public static Set<String> getClasses(String term, Language lang) {

        term = term.toLowerCase();
        Set<String> result = new HashSet<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconClassesEN.containsKey(term)) {
                    result.addAll(lexiconClassesEN.get(term));
                }
                break;
            case DE:
                if (lexiconClassesDE.containsKey(term)) {
                    result.addAll(lexiconClassesDE.get(term));
                }
                break;
            case ES:
                if (lexiconClassesES.containsKey(term)) {
                    result.addAll(lexiconClassesES.get(term));
                }
                break;
        }

        return result;
    }

    public static Set<String> getResources(String term, Language lang) {

        term = term.toLowerCase();
        Set<String> result = new HashSet<>();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconResourcesEN.containsKey(term)) {
                    result.addAll(lexiconResourcesEN.get(term));
                }
                break;
            case DE:
                if (lexiconResourcesDE.containsKey(term)) {
                    result.addAll(lexiconResourcesDE.get(term));
                }
                break;
            case ES:
                if (lexiconResourcesES.containsKey(term)) {
                    result.addAll(lexiconResourcesES.get(term));
                }
                break;
        }

        return result;
    }
}
