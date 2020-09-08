package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

//TODO: rename, resp. nejak sjednontit, protoze treba DateExtractor skutecne jen extrahuje, zatimco tohle extrahuje a zavoren uklada do solrInput
public class IdentifiersExtractor {

    public void extract(Element modsEl, SolrInput solrInput) {
        List<Node> identifierEls = Dom4jUtils.buildXpath("mods/identifier").selectNodes(modsEl);
        for (Node identifier : identifierEls) {
            String type = Dom4jUtils.stringOrNullFromAttributeByName((Element) identifier, "type");
            String value = toStringOrNull(identifier);
            if (type != null && value != null) {
                switch (type) {
                    //UNIVERSAL
                    case "urnnbn":
                    case "URNNBN":
                        solrInput.addField("n.id_urnnbn", normalizeIdValue(KnownIdType.URN_NBN, value));
                        break;
                    case "ccnb":
                    case "cnb":
                    case "čnb":
                    case "ččnb":
                    case "CCNB":
                    case "CNB":
                    case "ČNB":
                    case "ČČNB":
                        solrInput.addField("n.id_ccnb", normalizeIdValue(KnownIdType.CCNB, value));
                        break;
                    case "uuid":
                    case "UUID":
                        solrInput.addField("n.id_uuid", normalizeIdValue(KnownIdType.UUID, value));
                        break;
                    case "oclc":
                    case "OCLC":
                        solrInput.addField("n.id_oclc", normalizeIdValue(KnownIdType.OCLC, value));
                        break;
                    case "sysno":
                    case "SYSNO":
                        solrInput.addField("n.id_sysno", normalizeIdValue(KnownIdType.SYSNO, value));
                        break;
                    case "barCode":
                        solrInput.addField("n.id_barcode", normalizeIdValue(KnownIdType.BARCODE, value));
                        break;

                    //FOR SPECIFIC TYPES
                    case "isbn":
                    case "ISBN":
                        solrInput.addField("n.id_isbn", normalizeIdValue(KnownIdType.ISBN, value));
                        break;
                    case "issn":
                    case "ISSN":
                        solrInput.addField("n.id_issn", normalizeIdValue(KnownIdType.ISSN, value));
                        break;
                    case "ismn":
                    case "ISMN":
                        solrInput.addField("n.id_ismn", normalizeIdValue(KnownIdType.ISMN, value));
                        break;
                    default:
                        solrInput.addField("n.id_other", type + ':' + value);
                        break;
                }
            } else if (value != null) {//no type
                solrInput.addField("n.id_other", value);
            }
        }
    }

    private String normalizeIdValue(KnownIdType type, String value) {
        if (type != null) {
            switch (type) {
                case URN_NBN: {
                    value = value.toLowerCase();
                    if (value.startsWith("urnnbn:")) {
                        return value.substring("urnnbn:".length());
                    } else { //probably urn:nbn:cz:nk-000ktg
                        return value;
                    }
                }
                case CCNB: {
                    value = value.toLowerCase();
                    String[] prefixes = {"ccnb:", "cnb:", "ččnb:", "čnb:",};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case UUID: {
                    value = value.toLowerCase();
                    String[] prefixes = {"uuid:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case OCLC: {
                    value = value.toLowerCase();
                    String[] prefixes = {"oclc:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case SYSNO: {
                    value = value.toLowerCase();
                    String[] prefixes = {"sysno:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case BARCODE: {
                    String[] prefixes = {"barCode:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }

                case ISBN: {
                    value = value.toLowerCase();
                    String[] prefixes = {"isbn:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case ISSN: {
                    value = value.toLowerCase();
                    String[] prefixes = {"issn:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }
                case ISMN: {
                    value = value.toLowerCase();
                    String[] prefixes = {"ismn:"};
                    for (String prefix : prefixes) {
                        if (value.startsWith(prefix)) {
                            return value.substring(prefix.length());
                        }
                    }
                    return value;
                }

                default:
                    if (value.startsWith(type + ":")) {
                        return value;
                    } else {
                        return type + ":" + value;
                    }
            }
        } else {
            return value;
        }
    }


    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }


    public enum KnownIdType {
        URN_NBN, CCNB, UUID, OCLC,
        SYSNO, BARCODE,
        ISBN, ISSN, ISMN,

    }
}
