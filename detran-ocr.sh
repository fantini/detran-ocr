#!/bin/bash
export LC_NUMERIC="C"
java -jar -Djava.util.logging.SimpleFormatter.format='%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n' detranocr-jar-with-dependencies.jar 
