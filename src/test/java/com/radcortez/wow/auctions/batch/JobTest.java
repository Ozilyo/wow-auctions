package com.radcortez.wow.auctions.batch;

import com.radcortez.wow.auctions.business.WoWBusinessBean;
import com.radcortez.wow.auctions.entity.Realm;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static com.radcortez.wow.auctions.batch.BatchTestHelper.keepTestAlive;
import static org.junit.Assert.assertEquals;

/**
 * @author Roberto Cortez
 */
@RunWith(Arquillian.class)
public class JobTest {
    @Inject
    private WoWBusinessBean woWBusinessBean;

    @Deployment
    public static WebArchive createDeployment() {
        File[] requiredLibraries = Maven.resolver().loadPomFromFile("pom.xml")
                                        .resolve("commons-io:commons-io", "org.apache.commons:commons-lang3")
                                        .withTransitivity().asFile();

        WebArchive war = ShrinkWrap.create(WebArchive.class)
                                   .addAsLibraries(requiredLibraries)
                                   .addPackages(true, "com.radcortez.wow.auctions")
                                   .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                                   .addAsResource("META-INF/persistence.xml")
                                   .addAsResource("META-INF/sql/create.sql")
                                   .addAsResource("META-INF/sql/drop.sql")
                                   .addAsResource("META-INF/sql/load.sql")
                                   .addAsResource("META-INF/batch-jobs/loadRealms-job.xml");
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    @InSequence(1)
    public void testJob() throws Exception {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Long executionId = jobOperator.start("loadRealms-job", new Properties());

        JobExecution jobExecution = keepTestAlive(jobOperator, executionId);

        List<Realm> realms = woWBusinessBean.listReams();
        realms.forEach(System.out::println);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getBatchStatus());
    }
}
