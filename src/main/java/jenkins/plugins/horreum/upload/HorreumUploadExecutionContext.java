package jenkins.plugins.horreum.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.entity.json.Access;
import jenkins.model.Jenkins;
import jenkins.plugins.horreum.BaseExecutionContext;
import jenkins.plugins.horreum.HorreumGlobalConfig;
import jenkins.plugins.horreum.util.HttpRequestNameValuePair;

public class HorreumUploadExecutionContext extends BaseExecutionContext<String> {

	private static final long serialVersionUID = -2066857816168989599L;
	private static final String HORREUM_JENKINS_SCHEMA = "urn:horreum:jenkins-plugin:0.1";
	private final Map<String, HttpRequestNameValuePair> params;
	private final FilePath uploadFile;
	private final ObjectNode buildInfo;

	static HorreumUploadExecutionContext from(HorreumUploadConfig config,
															EnvVars envVars,
															Run<?, ?> run,
															TaskListener listener,
															Supplier<FilePath> filePathSupplier) {
		String url = envVars != null ? envVars.expand(HorreumGlobalConfig.get().getBaseUrl()) : HorreumGlobalConfig.get().getBaseUrl(); //http.resolveUrl(envVars, build, taskListener);
		List<HttpRequestNameValuePair> params = config.resolveParams(); //Need to define params in freestyle project
		FilePath uploadFile = filePathSupplier.get();
		TaskListener taskListener = config.getQuiet() ? TaskListener.NULL : listener;

		ObjectNode buildInfo = null;
		if (params.stream().anyMatch(p -> p.getName().equals("addBuildInfo") && "true".equals(p.getValue()))) {
			buildInfo = JsonNodeFactory.instance.objectNode();
			buildInfo.put("$schema", HORREUM_JENKINS_SCHEMA);
			buildInfo.put("buildUrl", Jenkins.get().getRootUrl() + run.getUrl());
			buildInfo.put("buildNumber", run.getNumber());
			buildInfo.put("buildDisplayName", run.getDisplayName());
			buildInfo.put("jobName", run.getParent().getName());
			buildInfo.put("jobDisplayName", run.getParent().getDisplayName());
			buildInfo.put("jobFullName", run.getParent().getFullName());
			buildInfo.put("scheduleTime", run.getTimeInMillis());
			buildInfo.put("startTime", run.getStartTimeInMillis());
			buildInfo.put("uploadTime", System.currentTimeMillis());
		}
		HorreumUploadExecutionContext context = new HorreumUploadExecutionContext(
				url,
				config.getCredentials(),
				params,
				uploadFile,
				buildInfo,
				taskListener.getLogger());
		return context;
	}

	private HorreumUploadExecutionContext(
			String url,
			String credentials,
			List<HttpRequestNameValuePair> params,
			FilePath uploadFile,
			ObjectNode buildInfo, PrintStream logger
	) {
		super(url, credentials, logger);
		this.params = new HashMap<>();
		params.forEach(param -> this.params.put(param.getName(), param));
		this.uploadFile = uploadFile;
		this.buildInfo = buildInfo;
	}

	@Override
	protected String invoke(HorreumClient client) {
		JsonNode json = null;
		try {
			json = new ObjectMapper().readTree(new File(uploadFile.getRemote()));
		} catch (IOException e) {
			throw new RuntimeException("File for upload cannot be read: " + uploadFile.getRemote(), e);
		}
		String schema = params.get("schema").getValue();
		if (buildInfo != null) {
			if (json.isArray()) {
				((ArrayNode) json).add(buildInfo);
			} else if (json.isObject()) {
				ObjectNode objectNode = (ObjectNode) json;
				if (schema != null && !schema.isEmpty()) {
					objectNode.put("$schema", schema);
				}
				if (objectNode.hasNonNull("$schema")) {
					ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
					arrayNode.add(json);
					arrayNode.add(buildInfo);
					json = arrayNode;
				} else {
					// the object is probably an aggregate of schemas
					objectNode.set("horreum-jenkins-plugin", buildInfo);
				}
			} else {
				ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
				arrayNode.add(json);
				arrayNode.add(buildInfo);
				json = arrayNode;
			}
		}
		String id = client.runService.addRunFromData(
				params.get("start").getValue(),
				params.get("stop").getValue(),
				params.get("test").getValue(),
				params.get("owner").getValue(),
				Access.valueOf(params.get("access").getValue()),
				null,
				"".equals(schema) ? null : schema,
				null,
				json
		);
		logger().printf("Uploaded run ID: %s%n", id);
		return id;
	}
}
