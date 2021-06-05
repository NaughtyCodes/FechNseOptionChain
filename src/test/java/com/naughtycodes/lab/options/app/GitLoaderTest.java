package com.naughtycodes.lab.options.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.aspectj.apache.bcel.util.ClassPath;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;


public class GitLoaderTest {
	
	public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {

		String dir = Arrays.asList(System.getProperty("java.class.path").split(";")).get(0);
		String[] str = dir.toString().split("lab_options");
		dir = dir.toString().replace(str[1],"");
		
		System.out.println("\n>>> Cloning repository\n");
		Repository repo = Git.open (new File(dir)).getRepository(); 

		try (Git git = new Git(repo)) {
			/*
			 * Stage modified files and Commit changes .
			 * 
			 * Equivalent of --> $ git commit -a
			 * 
			 */
			System.out.println("\n>>> Committing changes\n");
			RevCommit revCommit = git.commit().setAll(true).setMessage("Adding commit from JGIT").call();
			System.out.println("Commit = " + revCommit.getFullMessage());

			/*
			 * Verify commit log
			 * 
			 * Equivalent of --> $ git log
			 * 
			 */
			System.out.println("\n>>> Printing commit log\n");
			Iterable<RevCommit> commitLog = git.log().call();
			commitLog.forEach(r -> System.out.println(r.getFullMessage()));

		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
