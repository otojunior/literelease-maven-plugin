/**
 * 
 */
package br.org.otojunior.plugin.literelease;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * @author Oto Soares Coelho Junior (oto.coelho-junior@serpro.gov.br)
 *
 */
@Mojo(name="release")
public class ReleaseMojo extends AbstractMojo {
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
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Git git = Git.open(new File("./.git"));
			
			call(mavenVersionsSet(releaseVersion));
			jgitAdd(git);
			jgitCommit(git, releaseMessage);

			jgitTag(git);
			if (releaseDeploy) {
				call(mavenDeploy());
			}

			call(mavenVersionsSet(developmentVersion));
			jgitAdd(git);
			jgitCommit(git, developmentMessage);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 * @param cmd
	 * @throws MojoExecutionException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void call(String[] cmd) throws MojoExecutionException, IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(cmd);
		Process process = builder.start();
		print(process.getInputStream());
		process.waitFor();
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
			git.tag().setName(releaseVersion).call();
		} catch (GitAPIException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return
	 */
	private String[] mavenDeploy() {
		return new String[] { mavenExecutable, "clean", "deploy" };
	}

	/**
	 * 
	 * @param version
	 * @return
	 */
	private String[] mavenVersionsSet(String version) {
		return new String[] {
			mavenExecutable,
			"versions:set",
			"-DnewVersion=" + version,
			"-DgenerateBackupPoms=false"};
	}

	/**
	 * 
	 * @param inputStream
	 * @throws MojoExecutionException
	 */
	private void print(InputStream inputStream) throws MojoExecutionException {
		try (BufferedReader in =
				new BufferedReader(
				new InputStreamReader(inputStream))) {
			String line = in.readLine();
			while (line != null) {
				System.out.println(line);
				line = in.readLine();
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e); 
		}
	}
}
