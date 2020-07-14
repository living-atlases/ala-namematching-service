package au.org.ala.names.ws.resources;

import au.org.ala.names.ws.NameSearchConfiguration;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class NameSearchResourceTest {
    private NameSearchConfiguration configuration;
    private NameSearchResource resource;

    @Before
    public void setUp() throws Exception {
        this.configuration = new NameSearchConfiguration();
        this.configuration.setIndex("/data/lucene/namematching-20200214"); // Ensure consistent index
        this.resource = new NameSearchResource(this.configuration);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch1() throws Exception {
        NameUsageMatch match = this.resource.search("Acacia dealbata");
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification2() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Osphranter rufus").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testHomonym1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Codium sp.").genus("Codium").family("Alga").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("homonym"), match.getIssues());
    }

    @Test
    public void testHomonym2() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Agathis").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("homonym"), match.getIssues());
        search = NameSearch.builder().scientificName("Agathis").phylum("Arthropoda").build();
        match = this.resource.searchByClassification(search);
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:a4109d9e-723c-491a-9363-95df428fe230", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testMisapplied1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Corybas macranthus").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertFalse(match.isSuccess());
        assertEquals(Arrays.asList("homonym", "misappliedName"), match.getIssues());
    }

    @Test
    public void testSynonym1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Lepidium dubium").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/node/apni/2898429", match.getTaxonConceptID());
        assertEquals("wellformed", match.getNameType());
        assertEquals("SUBJECTIVE_SYNONYM", match.getSynonymType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testHybrid1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Eucalyptus globulus x Eucalyptus ovata").build();
        NameUsageMatch match = this.resource.searchByClassification(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/name/apni/152298", match.getTaxonConceptID());
        assertEquals("hybrid", match.getNameType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

}
