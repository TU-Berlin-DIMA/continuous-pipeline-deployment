setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)

urlDataProcessing <- function(){
  online = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))
  continuous = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))
  periodical = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))
  baseline = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))
  
  df = data.frame(Deployment = c('Online','Continuous', 'Periodical', 'Baseline'), 
                  Time = c(online$time, continuous$time, periodical$time,baseline$time))
  
  scale = 1000 * 60
  df$Time = df$Time / scale
  
  return (df)
}

taxiDataProcessing <- function(){
  online = sum(read.csv('nyc-taxi/deployment-modes/online/time', header = FALSE, col.names = c('time')))
  continuous = sum(read.csv('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/time', header = FALSE, col.names = c('time')))
  # PLACEHOLDER
  periodical = 5 * sum(read.csv('nyc-taxi/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time')))
  baseline = sum(read.csv('nyc-taxi/deployment-modes/baseline/time', header = FALSE, col.names = c('time')))
  
  df = data.frame(Deployment = c('Online','Continuous', 'Periodical', 'Baseline'), 
                  Time = c(online, continuous, periodical, baseline))
  
  scale = 1000 * 60
  df$Time = df$Time / scale
  return (df)
}

criteoDataProcessing <- function(){
  online = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))
  continuous = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))
  periodical = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))
  baseline = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))
  
  df = data.frame(Deployment = c('Online','Continuous', 'Periodical', 'Baseline'), 
                  Time = c(online$time, continuous$time, periodical$time,baseline$time))
  
  scale = 1000 * 60
  df$Time = df$Time / scale
  df$Time = 0
  return (df)
}


fontLabelSize = 14
baseSize = 20
rows = c(1,2,3)

####### URL PLOT ##########
urlData = urlDataProcessing()
urlPlot = ggbarplot(urlData[rows,], x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(a) URL",
          width = 1.0, size = 1.0,
          color = 'Deployment', fill = 'Deployment',
          order = c('Continuous','Periodical','Online'),
          ggtheme = theme_pubclean(base_size = baseSize)) + 
  geom_hline(aes(yintercept=criteoData[4,]$Time, linetype = 'Baseline'), size = 2, color = 'purple') + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)), 
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm"))+
  scale_linetype_manual("",values = c("dashed",NA,NA,NA))
urlPlot = ggpar(urlPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text') + rremove("legend") 
  
####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiPlot = ggbarplot(taxiData[rows,], x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(b) Taxi",
                     width = 1.0, size = 1.0,
                     color = 'Deployment', fill = 'Deployment',
                     order = c('Continuous','Periodical','Online'),
                     ggtheme = theme_pubclean(base_size = baseSize)) + 
  geom_hline(aes(yintercept=taxiData[4,]$Time, linetype = 'Baseline') , size = 2, color = 'purple') + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm")) +
  scale_linetype_manual("",values = c("dashed",NA,NA,NA)) 
taxiPlot = ggpar(taxiPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')

####### CRITEO PLOT ##########
criteoData = criteoDataProcessing()
criteoPlot = ggbarplot(criteoData[rows,], x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(c) Criteo",
                       width = 1.0, size = 1.0,
                       color = 'Deployment', fill = 'Deployment',
                       order = c('Continuous','Periodical','Online'),
                       #remove this
                       ylim=c(0,800),
                       ggtheme = theme_pubclean(base_size = baseSize)) + 
  geom_hline(aes(yintercept=criteoData[4,]$Time, linetype = 'Baseline') , size = 2, color = 'purple') + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)), 
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm")) +
  scale_linetype_manual("",values = c("dashed",NA,NA,NA))
criteoPlot = ggpar(criteoPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')


deploymentTime = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 1, ncol = 3, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/deployment-time-experiment.tex", width = 6, height = 2)
deploymentTime 
dev.off()

 
