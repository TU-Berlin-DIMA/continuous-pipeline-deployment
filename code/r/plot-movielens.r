setwd("~/Documents/work/phd-papers/continuous-training/code/python/continuous-training/results")

loadData <- function(file){
  library(readr)
  library(stringr)
  data = read_file(file)
  data = str_replace_all(data, " ", "")
  data = strsplit(data, ',')
  return(as.numeric(data[[1]]))
  
}



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

library(ggplot2)

p = 
  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e", fill = "Retraining"), 
             shape = 8, 
             lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    ", ""), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "black"))  +
  # legend for retraining point
  scale_fill_manual(name = "", values = c("Retraining" = "black")) + 
  # guides for enhancing legend
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 


ggsave(p , filename = 'movie-lens-100k/5000/movie-lens-100k-quality-improved.eps', device = 'eps', dpi = 1000)


# Plot Movie lens 1M data
continuous = loadData('movie-lens-1M/50000/continuous-error.txt')
velox = loadData('movie-lens-1M/50000/velox-error.txt')
baselinePlus = loadData('movie-lens-1M/50000/offline-online.txt')
baseline= loadData('movie-lens-1M/50000/offline-only.txt')


df = data.frame(time = 1:length(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(8379,16706,25075,33345,41765,50000)

library(ggplot2)

p = 
  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e", fill = "Retraining"), 
             shape = 8, 
             lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme(legend.text = element_text(size = 24), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    ", ""), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "black"))  +
  # legend for retraining point
  scale_fill_manual(name = "", values = c("Retraining" = "black")) + 
  # guides for enhancing legend
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 


ggsave(p , filename = 'movie-lens-1M/50000/movie-lens-1m-quality-improved.eps', device = 'eps', dpi = 1000)



# Plot buffer size vs time

times = loadData('movie-lens-100k/buffer-size/time.txt')
buffer = seq(500, 5000, 500)

df = data.frame(buffer, times = times/60)

bufferVsTimePlot = ggplot(data = df) + 
  aes(x = buffer, y = times, colour = "blue") + 
  geom_line(size = 1.5) + 
  geom_point(lwd = 4)  + 
  xlab("Buffer size") + ylab("Time (m)") + 
  scale_color_manual(values=c("blue" = "blue")) + 
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  guides(colour=FALSE)

ggsave(bufferVsTimePlot , filename = 'movie-lens-100k/buffer-size/movie-lens-100k-buffer-time-improved.eps', device = 'eps', dpi = 1000)

# Plot sampling rate vs time
times = loadData('movie-lens-100k/sampling/')
buffer = seq(500, 5000, 500)

df = data.frame(buffer, times = times/60)

bufferVsTimePlot = ggplot(data = df) + 
  aes(x = buffer, y = times, colour = "blue") + 
  geom_line(size = 1.5) + 
  geom_point(lwd = 4)  + 
  xlab("Buffer size") + ylab("Time (m)") + 
  scale_color_manual(values=c("blue" = "blue")) + 
  theme(axis.text=element_text(size=20),
        axis.title=element_text(size=28)) + 
  guides(colour=FALSE)

ggsave(bufferVsTimePlot , filename = 'movie-lens-100k/buffer-size/movie-lens-100k-buffer-time-improved.eps', device = 'eps', dpi = 1000)