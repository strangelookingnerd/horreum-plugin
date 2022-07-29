package jenkins.plugins.horreum.upload;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.AbstractIdCredentialsListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import jenkins.plugins.horreum.HorreumBaseBuilder;
import jenkins.plugins.horreum.HorreumBaseDescriptor;
import jenkins.plugins.horreum.HorreumGlobalConfig;

//TODO: Make safe functionality as upload step
public class HorreumUpload extends HorreumBaseBuilder<HorreumUploadConfig> {
	@DataBoundConstructor
	public HorreumUpload(@Nonnull String credentials,
								@Nonnull String test,
								@Nonnull String owner,
								@Nonnull String access,
								@Nonnull String start,
								@Nonnull String stop,
								@Nonnull String schema,
								@Nonnull String jsonFile,
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

	@DataBoundSetter
	public void setSchema(String schema) {
		this.config.setSchema(schema);
	}

	public String getSchema() {
		return config.getSchema();
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
	protected HorreumUploadExecutionContext createExecutionContext(AbstractBuild<?, ?> build, BuildListener listener, EnvVars envVars) {
		return HorreumUploadExecutionContext.from(config, envVars, build,
				listener, () -> this.config.resolveUploadFile(envVars, build));
	}

	@Extension
	public static final class DescriptorImpl extends HorreumBaseDescriptor {
		public static final String jsonFile = "";

		public DescriptorImpl() {
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Horreum Upload";
		}

		public ListBoxModel doFillAccessItems(@AncestorInPath Item item, @QueryParameter String credentials) {
			ListBoxModel items = new ListBoxModel();
			items.add("PUBLIC");
			items.add("PROTECTED");
			items.add("PRIVATE");
			return items;
		}
	}
}
