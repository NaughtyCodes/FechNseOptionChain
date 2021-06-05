package com.naughtycodes.lab.options.app.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;

@Component
public class GitConfig {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(GitConfig.class);
	
	public void pushToGit() throws IOException, InvalidRemoteException, TransportException, GitAPIException {

//		String dir = Arrays.asList(System.getProperty("java.class.path").split(";")).get(0);
//		String[] str = dir.toString().split("lab_options");
//		dir = dir.toString().replace(str[1],"");
		
		System.out.println("\n>>> Cloning repository\n");
		Repository repo = Git.open (new File("F:\\lab\\eclipse-workspace\\NseOptionsChainData")).getRepository(); 

		try (Git git = new Git(repo)) {
			/*
			 * Stage modified files and Commit changes .
			 * 
			 * Equivalent of --> $ git commit -a
			 * 
			 */
			System.out.println("\n>>> Committing changes\n");
			Thread.sleep(5000);
			git.add().addFilepattern(".").call();
			RevCommit revCommit = git.commit().setAll(true).setMessage("Adding commit from JGIT__"+java.time.LocalTime.now()).call();
			System.out.println("Commit = " + revCommit.getFullMessage());
			CredentialsProvider cp = new UsernamePasswordCredentialsProvider("hellomohanakrishnan@gmail.com", "lab@2021");
			Iterable<PushResult> pushCommands = git.push().setCredentialsProvider(cp).call();
			pushCommands.forEach(r -> System.out.println(r.getMessages()));
			
			/*
			 * Verify commit log
			 * 
			 * Equivalent of --> $ git log
			 * 
			 *
			 *	System.out.println("\n>>> Printing commit log\n");
			 *	Iterable<RevCommit> commitLog = git.log().call();
			 *	commitLog.forEach(r -> System.out.println(r.getFullMessage()));
			 *
			 */
		
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
