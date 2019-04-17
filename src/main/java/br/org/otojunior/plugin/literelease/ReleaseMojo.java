/**
 * 
 */
package br.org.otojunior.plugin.literelease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
			call(mavenVersionsSet(releaseVersion));
			call(gitAdd());
			call(gitCommit(releaseMessage));

			call(gitTag());
			if (releaseDeploy) {
				call(mavenDeploy());
			}

			call(mavenVersionsSet(developmentVersion));
			call(gitAdd());
			call(gitCommit(developmentMessage));
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
	 * @param version
	 * @return
	 */
	private String[] gitAdd() {
		return new String[] { gitExecutable, "add", "-A" };
	}

	/**
	 * 
	 * @param version
	 * @return
	 */
	private String[] gitCommit(String message) {
		return new String[] { gitExecutable, "commit", "-m", message };
	}
	
	/**
	 * 
	 * @return
	 */
	private String[] gitTag() {
		return new String[] { gitExecutable, "tag", releaseVersion };
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
