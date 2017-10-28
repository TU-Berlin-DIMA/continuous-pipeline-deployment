setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)


appendThenSample = read.csv('sampling-mode/local/append-then-sample/loss', header = FALSE, col.names = c('appendThenSample'))
sampleThenAppend = read.csv('sampling-mode/local/sample-then-append/', header = FALSE, col.names = c('sampleThenAppend'))

df = data.frame(time = 1:nrow(appendThenSample),
                appendThenSample = appendThenSample,
                sampleThenAppend = sampleThenAppend)

ml = melt(df, id.vars = 'time')
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) 