setwd("~/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/results/criteo-sample/processed/")

data_continuous = read.csv(file = 'continuous/2017-02-06-17-00/error-rates.txt', header = FALSE)
data_velox =read.csv(file = 'velox/2017-02-06-17-17/error-rates.txt', header = FALSE)
data_streaming =read.csv(file = 'online/2017-02-06-17-34/error-rates.txt', header = FALSE)
data_initial =read.csv(file = 'initial-only/2017-02-06-17-52/error-rates.txt', header = FALSE)


plot(1:dim(data_velox)[1], data_velox$V1, type = 'l' , col = 'red', xlim = c(1,max(dim(data_velox)[1], dim(data_velox)[1])), ylim = c(0.2,0.5))
lines(1:dim(data_continuous)[1], data_continuous$V1, col = 'blue')
lines(1:dim(data_streaming)[1], data_streaming$V1, col= 'green')
lines(1:dim(data_initial)[1], data_initial$V1, col= 'darkgoldenrod4')

legend("topright", col = c("red", "blue", "green", "darkgoldenrod4"), lty = 1,legend=c("velox", "continuous", "online", 'initial'))



# Plot Movie lens 100 K data
continuous = loadData('movie-lens-100k/5000/continuous-error.txt')
velox = loadData('movie-lens-100k/5000/velox-error.txt')
baselinePlus = loadData('movie-lens-100k/5000/offline-online.txt')
baseline= loadData('movie-lens-100k/5000/offline-only.txt')


df = data.frame(time = 1:length(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(830,1699,2485,3355,4204,5000)

ml = melt(df, id.vars = 'time' )
ggplot() + 
  geom_line(data = ml , aes( x = time, y = value, group = variable, colour = variable)) + 
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, fill = "retraining"), 
             shape = 2, 
             lwd = 4 ) +
  xlab("Testing Increments") + ylab("Mean Squared Error") + 
  scale_color_discrete("", labels =c("continuous", "velox", "baseline", "baseline+")) + 
  scale_fill_discrete("") + 
  theme_bw() + 
  theme(legend.text = element_text(size = 26), 
        legend.title = element_text(size = 26), 
        legend.key.size  = unit(1.0, "cm")) +
  theme(axis.text=element_text(size=26),
        axis.title=element_text(size=28)) + 
  theme(legend.text = element_text(size = 26), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") 


p = 
  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a")) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b")) + 
  geom_line(aes(x = time, y  = continuous, colour = "c")) + 
  geom_line(aes(x = time, y  = velox, colour = "d")) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e", fill = "Retraining"), 
             shape = 24, 
             lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 26), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=26),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    ", ""), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "red"))  +
  # legend for retraining point
  scale_fill_manual(name = "", values = c("Retraining" = "red")) + 
  # guides for enhancing legend
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 