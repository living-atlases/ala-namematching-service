package au.org.ala.names.ws.resources;

import au.org.ala.names.model.LinnaeanRankClassification;
import au.org.ala.names.model.MatchType;
import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.ws.api.NameSearch;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class NameSearchResource {

    private Logger LOG = LoggerFactory.getLogger("mylogger");

    private ALANameSearcher searcher = null;

    public NameSearchResource(){
        try {
            searcher = new ALANameSearcher("/data/lucene/namematching");
        } catch (Exception e){
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("Unable to initialise searcher: " + e.getMessage(), e);
        }
    }

    @GET
    @Timed
    public NameSearch search(@QueryParam("q") String name) {
        try {
            NameSearchResult nsr = searcher.searchForRecord(name);
            if (nsr != null){
                MatchType matchType = nsr.getMatchType();
                if (nsr.getAcceptedLsid() != null && nsr.getLsid() != nsr.getAcceptedLsid()){
                    nsr = searcher.searchForRecordByLsid(nsr.getAcceptedLsid());
                }
                Set<String> vernacularNames = searcher.getCommonNamesForLSID(nsr.getLsid(), 1);
                return create(nsr, vernacularNames, matchType);
            } else {
                return NameSearch.FAIL;
            }
        } catch (Exception e){
            LOG.warn("Problem matching name : " + e.getMessage() + " with name: " + name);
        }
        return NameSearch.FAIL;
    }

    private NameSearch create(NameSearchResult nsr, Set<String> vernacularNames, MatchType matchType){
        if(nsr != null && nsr.getRankClassification() != null){
            LinnaeanRankClassification lrc = nsr.getRankClassification();
            return NameSearch.builder()
                    .success(true)
                    .scientificName(lrc.getScientificName())
                    .scientificNameAuthorship(lrc.getAuthorship())
                    .taxonConceptID(nsr.getLsid())
                    .rank(nsr.getRank().getRank())
                    .matchType(matchType != null ? matchType.toString() : "")
                    .left(nsr.getLeft() != null ? Integer.parseInt(nsr.getLeft()) : null)
                    .right(nsr.getRight() != null ? Integer.parseInt(nsr.getRight()) : null)
                    .kingdom(lrc.getKingdom())
                    .kingdomID(lrc.getKid())
                    .phylum(lrc.getPhylum())
                    .phylumID(lrc.getPid())
                    .classs(lrc.getKlass())
                    .classID(lrc.getCid())
                    .order(lrc.getOrder())
                    .orderID(lrc.getOid())
                    .family(lrc.getFamily())
                    .familyID(lrc.getFid())
                    .genus(lrc.getGenus())
                    .genusID(lrc.getGid())
                    .species(lrc.getSpecies())
                    .speciesID(lrc.getSid())
                    .vernacularName(!vernacularNames.isEmpty() ? vernacularNames.iterator().next() : null)
                    .build();
        } else {
            return NameSearch.FAIL;
        }
    }
}
