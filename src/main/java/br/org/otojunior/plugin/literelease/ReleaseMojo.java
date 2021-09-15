/**
 * 
 */
package br.org.otojunior.plugin.literelease;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * @author Oto Soares Coelho Junior (oto.coelho-junior@serpro.gov.br)
 *
 */
@Mojo(name="release")
public class ReleaseMojo extends AbstractMojo {
	/**
	 * 
	 */
	@Component
	private MavenProject mavenProject;

	/**
	 * 
	 */
	@Component
	private MavenSession mavenSession;

	/**
	 * 
	 */
	@Component
	private BuildPluginManager pluginManager;

	/**
	 * 
	 */
	@Parameter(property="development.messsage", required=false, defaultValue="[Next Development]")
	private String developmentMessage;

	/**
	 * 
	 */
	@Parameter(property="development.version", required=true)
	private String developmentVersion;

	/**
	 * 
	 */
	@Parameter(property="git.executable", required=false, defaultValue="git")
	private String gitExecutable;

	/**
	 * 
	 */
	@Parameter(property="maven.executable", required=false, defaultValue="mvn")
	private String mavenExecutable;

	/**
	 * 
	 */
	@Parameter(property="release.deploy", required=false, defaultValue="true")
	private boolean releaseDeploy;

	/**
	 * 
	 */
	@Parameter(property="release.messsage", required=false, defaultValue="[Release]")
	private String releaseMessage;

	/**
	 * 
	 */
	@Parameter(property="release.version", required=true)
	private String releaseVersion;

	/**
	 * 
	 */
	@Parameter(property="tag.name", required=true, defaultValue="${release.version}")
	private String tagname;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			File gitDirectory = new File(mavenProject.getBasedir(), ".git");
			try (Git git = Git.open(gitDirectory)) {
				goalSetVersion(releaseVersion);
				jgitAdd(git);
				jgitCommit(git, releaseMessage);

				jgitTag(git);
				if (releaseDeploy) {
					goalClean();
					goalDeploy();
				}

				goalSetVersion(developmentVersion);
				jgitAdd(git);
				jgitCommit(git, developmentMessage);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @throws MojoExecutionException
	 */
	private void goalClean() throws MojoExecutionException {
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-clean-plugin"),
				version("3.1.0")
		    ),
			goal("clean"),
			configuration(
				element(MojoExecutor.name("skip"), "false")
		    ),
			executionEnvironment(
		        mavenProject,
		        mavenSession,
		        pluginManager
		    )
		);
	}

	/**
	 * 
	 * @throws MojoExecutionException
	 */
	private void goalDeploy() throws MojoExecutionException {
		InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(mavenProject.getModel().getPomFile());
        request.setGoals(Collections.singletonList("deploy"));

        Invoker invoker = new DefaultInvoker();
        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
	}

	/**
	 * 
	 * @param version
	 * @throws MojoExecutionException
	 */
	private void goalSetVersion(String version) throws MojoExecutionException {
		executeMojo(
			plugin(
				groupId("org.codehaus.mojo"),
				artifactId("versions-maven-plugin"),
				version("2.8.1")
		    ),
			goal("set"),
			configuration(
				element(MojoExecutor.name("newVersion"), version),
				element(MojoExecutor.name("generateBackupPoms"), "false")
		    ),
			executionEnvironment(
		        mavenProject,
		        mavenSession,
		        pluginManager
		    )
		);
	}

	/**
	 * 
	 * @param git
	 * @throws MojoExecutionException
	 */
	private void jgitAdd(Git git) throws MojoExecutionException {
		try {
			git.add().addFilepattern(".").call();
			git.add().setUpdate(true).addFilepattern(".").call();
		} catch (GitAPIException e) {
			throw new MojoExecutionException(e.getMessage(), e); 
		}
	}

	/**
	 * 
	 * @param message
	 * @throws MojoExecutionException 
	 */
	private void jgitCommit(Git git, String message) throws MojoExecutionException {
		try {
			CommitCommand commit = git.commit();
			commit.setMessage(message).call();
		} catch (GitAPIException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param git
	 * @throws MojoExecutionException
	 */
	private void jgitTag(Git git) throws MojoExecutionException {
		try {
			git.tag().setName(tagname).call();
		} catch (GitAPIException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
