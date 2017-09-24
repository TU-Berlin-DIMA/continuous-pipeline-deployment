setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

daily = read.csv('quality/experiment-quality-0.1-l2-500.txt', header = FALSE, col.names = c('day','fraction','logloss'))

continuous = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-adam/step-size-1.0/2017-09-22-17-15/loss.txt', header = FALSE, col.names = 'continuous')


ggplot() +
  geom_point(data = daily, aes(x = day, y = logloss,color='retraining'), pch=16, size = 5) + 
  geom_line(data=continuous, aes(x=seq(0.0,4.9,0.1), y=continuous, color='continuous')) + 
  xlab("Day") + ylab("LogLoss") +
  labs(title="Logistic Loss for Criteo Dataset") 
  


  
  
proactive = read.csv('mini-batch-only/loss.txt', header = FALSE, col.names = 'proactive')

baseline= read.csv('mini-batch-only/loss.txt', header = FALSE, col.names = 'baseline')

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                baseline = baseline[[1]][1:20],
                proactive = proactive)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Logistic Loss")

loss = read.csv('quality/loss/full/regularized/day-4-.001.txt', header = FALSE, col.names = c('iter','loss'))
loss4 = read.csv('quality/loss/full/regularized/day-4-.01.txt', header = FALSE, col.names = c('iter','loss'))
loss5 = read.csv('quality/loss/full/regularized/day-4-.1.txt', header = FALSE, col.names = c('iter','loss'))

df = data.frame(iter = loss$iter, day0=loss$loss, day4=loss4$loss, day5=loss5$loss)

allLoss = melt(df, id.vars = 'iter' )
ggplot (data = allLoss, aes(x = iter, y = value, group = variable)) +
  geom_line(aes(color = variable))

