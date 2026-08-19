#!/usr/bin/env node
'use strict';

// Runs as the detached top-level process for a managed dev service.
//
// On Windows, a *detached* process whose immediate child is cmd.exe (i.e.
// spawning with { shell: true, detached: true } directly) silently loses
// stdio redirected to a file once the command hops through another .cmd
// shim (npm.cmd -> node -> vite, etc.) - the file stays empty even though
// the process tree is alive and working. Making node.exe itself the
// detached top-level process, which then spawns the real shell command as
// an ordinary (non-detached) child, avoids that: node -> cmd -> npm -> vite
// reliably inherits the redirected stdio at every hop.
const { spawn } = require('node:child_process');

const command = process.argv[2];

const child = spawn(command, { shell: true, stdio: 'inherit', cwd: process.cwd() });

child.on('exit', (code, signal) => {
  process.exit(signal ? 1 : (code ?? 0));
});

process.on('SIGTERM', () => child.kill('SIGTERM'));
process.on('SIGINT', () => child.kill('SIGINT'));
