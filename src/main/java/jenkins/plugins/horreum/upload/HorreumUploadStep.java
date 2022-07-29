package jenkins.plugins.horreum.upload;

import java.io.IOException;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import jenkins.plugins.horreum.HorreumBaseStep;

public final class HorreumUploadStep extends HorreumBaseStep<HorreumUploadConfig> {

	@DataBoundConstructor
	public HorreumUploadStep(String credentials,
									 String test,
									 String owner,
							   	 String access,
									 String start,
									 String stop,
									 String schema,
									 String jsonFile,
									 boolean addBuildInfo) {

		super(new HorreumUploadConfig(credentials, test, owner, access, start, stop, schema, jsonFile, addBuildInfo));
	}

	public String getTest() {
		return config.getTest();
	}

	@DataBoundSetter
	public void setTest(String test) {
		this.config.setTest(test);
	}

	public String getOwner() {
		return config.getOwner();
	}

	@DataBoundSetter
	public void setOwner(String owner) {
		this.config.setOwner(owner);
	}

	public String getAccess() {
		return this.config.getAccess();
	}

	@DataBoundSetter
	public void setAccess(String access) {
		this.config.setAccess(access);
	}

	public String getStart() {
		return config.getStart();
	}

	@DataBoundSetter
	public void setStart(String start) {
		this.config.setStart(start);
	}

	public String getStop() {
		return config.getStop();
	}

	@DataBoundSetter
	public void setStop(String stop) {
		this.config.setStop(stop);
	}

	public String getSchema() {
		return config.getSchema();
	}

	@DataBoundSetter
	public void setSchema(String schema) {
		this.config.setSchema(schema);
	}

	public String getJsonFile() {
		return config.getJsonFile();
	}

	@DataBoundSetter
	public void setJsonFile(String jsonFile) {
		this.config.setJsonFile(jsonFile);
	}

	public boolean getAddBuildInfo() {
		return config.getAddBuildInfo();
	}

	@DataBoundSetter
	public void setAddBuildInfo(boolean add) {
		config.setAddBuildInfo(add);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
		public static final String jsonFile = HorreumUpload.DescriptorImpl.jsonFile;

		public DescriptorImpl() {
			super(Execution.class);
		}

		@Override
		public String getFunctionName() {
			return "horreumUpload";
		}

		@Override
		public String getDisplayName() {
			return "Upload a JSON object to a Horreum instance";
		}

		public ListBoxModel doFillAuthenticationItems(
				@AncestorInPath Item project, @QueryParameter String url) {
			return HorreumBaseDescriptor.fillAuthenticationItems(project, url);
		}
	}

	public static final class Execution extends HorreumBaseStep.Execution<String> {
		@Inject
		private transient HorreumUploadStep step;

		@Override
		protected BaseExecutionContext<String> createExecutionContext() throws Exception {
			StepContext context = getContext();
			return HorreumUploadExecutionContext.from(step.config, null, context.get(Run.class), context.get(TaskListener.class), this::resolveUploadFile);
		}

		private static final long serialVersionUID = 1L;

		private FilePath resolveUploadFile() {
			String uploadFile = step.getJsonFile();
			if (uploadFile.trim().isEmpty()) {
				return null;
			}

			try {
				FilePath workspace = getContext().get(FilePath.class);
				if (workspace == null) {
					throw new IllegalStateException("Could not find workspace to check existence of upload file: " + uploadFile +
							". You should use it inside a 'node' block");
				}
				FilePath uploadFilePath = workspace.child(uploadFile);
				if (!uploadFilePath.exists()) {
					throw new IllegalStateException("Could not find upload file: " + uploadFile);
				}
				return uploadFilePath;
			} catch (IOException | InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
