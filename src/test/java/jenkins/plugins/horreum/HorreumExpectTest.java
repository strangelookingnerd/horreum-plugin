package jenkins.plugins.horreum;

import static io.hyperfoil.tools.HorreumTestClientExtension.dummyTest;
import static io.hyperfoil.tools.HorreumTestClientExtension.horreumClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.hyperfoil.tools.horreum.entity.alerting.RunExpectation;
import jenkins.plugins.horreum.expect.HorreumExpect;

public class HorreumExpectTest extends HorreumPluginTestBase {
	@Test
	public void testExpectRun() throws Exception {
		HorreumExpect horreumExpect = new HorreumExpect(
				HORREUM_UPLOAD_CREDENTIALS, dummyTest.name, 60, "Jenkins CI", "$BUILD_URL"
		);

		// Run build
		FreeStyleProject project = this.j.createFreeStyleProject("Horreum-Expect-Freestyle");
		project.getBuildersList().add(horreumExpect);
		FreeStyleBuild build = project.scheduleBuild2(0).get();

		// Check expectations
		j.assertBuildStatusSuccess(build);

		List<RunExpectation> expectations = horreumClient.alertingService.expectations();
		expectations = expectations.stream().filter(e -> e.testId == dummyTest.id).collect(Collectors.toList());
		assertEquals(1, expectations.size());
		RunExpectation runExpectation = expectations.get(0);
		assertEquals("Jenkins CI", runExpectation.expectedBy);
		assertTrue(runExpectation.backlink.contains("jenkins/job/Horreum-Expect-Freestyle"));
	}
}
