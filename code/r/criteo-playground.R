setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)

adam = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-adam/step-size-0.001/2017-09-26-00-08/loss.txt', header = FALSE, col.names = 'adam')
rmsprop = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-rmsprop/step-size-1.0/2017-09-26-01-17/loss.txt', header = FALSE, col.names = 'rmsprop')
decreasing = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-l2/step-size-0.001/2017-09-24-21-48/loss.txt', header = FALSE, col.names = 'decreasing')
adadelta = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-adadelta/step-size-0.001/2017-09-24-21-34/loss.txt', header = FALSE, col.names = 'adadelta')
momentum = read.csv('continuous/model-type-lr/num-iterations-500/slack-10/updater-momentum/step-size-0.01/2017-09-24-21-00/loss.txt', header = FALSE, col.names = 'momentum')

df = data.frame(time = 1:nrow(adam),
                rmsprop = rmsprop, 
                #decreasing = decreasing,
                #adadelta = adadelta,
                adam = adam)
                #momentum=momentum)

ml = melt(df, id.vars = 'time' )
pl = ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Logistic Loss") +
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) 

ggsave(pl , filename = 'criteo-learning-rate-comparison.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")

daily = read.csv('quality/experiment-quality-0.1-rmsprop-500.txt', header = FALSE, col.names = c('day','fraction','logloss'))


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
pl = ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Logistic Loss")

ggsave(pl , filename = 'criteo-log-loss-continuous-vs-daily.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


loss = read.csv('quality/loss/full/regularized/day-4-.001.txt', header = FALSE, col.names = c('iter','loss'))
loss4 = read.csv('quality/loss/full/regularized/day-4-.01.txt', header = FALSE, col.names = c('iter','loss'))
loss5 = read.csv('quality/loss/full/regularized/day-4-.1.txt', header = FALSE, col.names = c('iter','loss'))

df = data.frame(iter = loss$iter, day0=loss$loss, day4=loss4$loss, day5=loss5$loss)

allLoss = melt(df, id.vars = 'iter' )
ggplot (data = allLoss, aes(x = iter, y = value, group = variable)) +
  geom_line(aes(color = variable))


