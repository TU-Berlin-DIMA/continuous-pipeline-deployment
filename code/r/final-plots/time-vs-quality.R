setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')


urlDataProcessing <- function(){
  scale = 1000 * 60
  oMC = mean(getMisclassification('url-reputation/final/deployment-modes/online/confusion_matrix')) * 100
  oTime = sum(read.csv('url-reputation/final/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time / scale)
  cMC = mean(getMisclassification('url-reputation/final/deployment-modes/continuous-with-optimization-time_based-100/confusion_matrix'))* 100
  cTime = sum(read.csv('url-reputation/final/deployment-modes/continuous-with-optimization-time_based-100/time', header = FALSE, col.names = c('time'))$time / scale)
  pMC = mean(getMisclassification('url-reputation/final/deployment-modes/periodical-with-warmstarting/confusion_matrix'))* 100
  pTime = sum(read.csv('url-reputation/final/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time / scale)
  
  df = data.frame(Time = c(oTime, pTime,cTime),
                  MC = c(oMC, pMC, cMC),
                  Deployment = factor(c('Online','Retraining', 'Proactive'), levels = c('Proactive','Retraining','Online')))
  return(df)
}


taxiDataProcessing <- function(){
  scale = 1000 * 60
  oMC = mean(getRMSLE('nyc-taxi/final/deployment-modes/online/rmsle'))
  oTime = sum(read.csv('nyc-taxi/final/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time / scale)
  cMC = mean(getRMSLE('nyc-taxi/final/deployment-modes/continuous-with-optimization-time_based-720/rmsle'))
  cTime = sum(read.csv('nyc-taxi/final/deployment-modes/continuous-with-optimization-time_based-720/time', header = FALSE, col.names = c('time'))$time / scale)
  pMC = mean(getRMSLE('nyc-taxi/final/deployment-modes/periodical-with-warmstarting/rmsle'))
  pTime = sum(read.csv('nyc-taxi/final/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time / scale)
  
  df = data.frame(Time = c(oTime, pTime,cTime),
                  MC = c(oMC, pMC, cMC),
                  Deployment = factor(c('Online','Retraining', 'Proactive'), levels = c('Proactive','Retraining','Online')))
  return(df)
}

# For the paper use
# fontLabelSize = 12
# baseSize = 14
# margin = -1
# loc = 'tikz'

# For presentation use
fontLabelSize = 30
baseSize = 34
margin = 3
loc = 'slides'



####### URL PLOT ##########
urlBreaks = c(0, 400 ,800)
urlData = urlDataProcessing()
urlPlot = ggscatter(urlData, x = "Time", 
          y= "MC", 
          color = "Deployment", 
          shape = "Deployment", size = 15, ylim=c(2.23,2.27), xlim=c(-20,1000),
          ylab = 'Misclassification %', xlab = "Time(m)",
          ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.8,'cm'),
        legend.key.height = unit(0.8,'cm'),
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

urlPlot = ggpar(urlPlot, legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize))

####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiBreaks = c(0 ,1000, 2000)
taxiBreaksY = c(0.0974,0.0975, 0.0976)
taxiPlot = ggscatter(taxiData, x = "Time", 
                    y= "MC", 
                    color = "Deployment", 
                    shape = "Deployment", size = 15, ylim = c(0.0974,0.0976), xlim=c(0,2000),
                    ylab = 'RMSLE', xlab = "Time(m)",
                    ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks) + 
  scale_y_continuous(breaks = taxiBreaksY)+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.8,'cm'),
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
         axis.title.y = element_text(margin = margin(r=margin)),
         axis.text.x = element_text(margin = margin(t=margin)))
taxiPlot = ggpar(taxiPlot, legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize))

####### CRITEO PLOT ##########
criteoData = urlDataProcessing()
criteoBreaks = c(0, 400 ,800)
criteoPlot = ggscatter(criteoData, x = "Time", 
                    y= "MC", 
                    color = "Deployment", 
                    shape = "Deployment", size = 2.5 ,ylim = c(2.14,2.22),
                    ylab = 'LogLoss', xlab = "Time(m)\n(c) Criteo",
                    ggtheme = theme_pubclean(base_size = baseSize)) +
  scale_x_continuous(breaks = criteoBreaks) +
  theme(legend.title = element_text(size = 0), 
         plot.margin = unit(c(0,1.5,0,0), "lines"), 
         axis.title.y = element_text(margin = margin(r=margin)),
         axis.text.x = element_text(margin = margin(t=margin)))
criteoPlot = ggpar(criteoPlot, legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize))

#qualityVsTime = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 1, ncol = 3, common.legend = TRUE)
qualityVsTime = ggarrange(urlPlot, taxiPlot, nrow = 1, ncol = 2, common.legend = TRUE)

#ggsave(qualityVsTime, filename = paste('../images/experiment-results/',loc, '/quality-vs-time.eps', sep=''), device = 'eps', width = 8, height = 4, units = "in")
ggsave(urlPlot, filename = paste('../images/experiment-results/',loc, '/url-quality-vs-time.pdf', sep=''), device = 'pdf', width = 8, height = 8, units = "in")
ggsave(taxiPlot, filename = paste('../images/experiment-results/',loc, '/taxi-quality-vs-time.pdf', sep=''), device = 'pdf', width = 8, height = 8, units = "in")
#tikz(file = "../images/experiment-results/tikz/quality-vs-time.tex", width = 4, height = 2)
#qualityVsTime 
#dev.off()