#!/usr/bin/env Rscript

# ------------------------------------------------------------------------------------------------ #
# Adapted from original BUSCO plotting script
#
# I cleaned up the code a little bit to no longer need a python script to build the R-script.
# All this scripts needs is the BUSCO short summary files to be in the same directory as this script
# (which is the case via the Nextflow pipeline). It will then match any files in this directory that
# end in '.txt', read each line of each file in as a vector, pull out the information and plot it
# in the exact same way as the original script, just using cleaned up code.

# ------------------------------------------------------------------------------------------------ #
# Libraries
suppressPackageStartupMessages({
  library(here)
  library(ragg)
  library(fs)
  library(stringr)
  library(purrr)
  library(readr)
  library(tibble)
  library(dplyr)
  library(magrittr)
  library(ggplot2)
})

print('1. Packages loaded')

# ------------------------------------------------------------------------------------------------ #
# Output parameters
my_ouput <- here('busco_figure.png')
my_width <- 20
my_height <- 15
my_unit <- "cm"

# Colors
my_colors <- c("#56B4E9", "#3492C7", "#F0E442", "#F04442")

# Bar height ratio
my_bar_height <- 0.75

# Legend
my_title <- "BUSCO Assessment Results\n"

# Font
my_family <- "sans"
my_size_ratio <- 1
labsize = 1

# ------------------------------------------------------------------------------------------------ #
# Data input
files <- dir_ls(
  path = here('.'),
  glob = '*.txt'
) %>%
  as.character() %>%
  set_names(sub('.+_odb10.(.*).txt', '\\1', basename(.)))

print(paste0('2. Found ', length(files), ' BUSCO short-summary files'))

dat <- files %>%
  imap_dfr(.id = 'assembly', ~{
    l <- read_lines(.x)

    # Get summary
    sm <- l[9]
    sm <- gsub('\\t|\\t\\s+', '', sm)

    # Get total number of genes in ortholog set
    n <- as.integer(as.character(sub('.+n:(.*)$', '\\1', sm)))

    # Get table of results
    l[10:15] %>%
      sub('\\t', '', .) %>%
      map_dfr(function(v){
        line <- unlist(str_split(string = v, pattern = '\\t', n = 2))
        tibble(
          measure = line[2],
          value = line[1]
        ) %>%
          mutate(
            measure = sub('\\t.*', '', measure)
          )
      }) %>%
      mutate(
        summary = sm,
        ntotal = n
      )
  }) %>%
  mutate(
    value = as.integer(as.character(value)),
    perc = value/ntotal * 100,
    category = str_extract(string = measure, pattern = '\\(.*\\)'),
    category = gsub('\\(|\\)', '', category),
    assembly = factor(x = assembly, levels = unique(assembly)),
    category = factor(x = category, levels = c('C', 'S', 'D', 'F', 'M'))
  ) %>%
  filter(
    !is.na(category),
    measure != 'Complete BUSCOs (C)'
  )

print('3. Short-summary files successfully imported')

# ------------------------------------------------------------------------------------------------ #
# Plot
fig <- dat %>%
  ggplot() +
  geom_bar(
    aes(
      y = perc,
      x = assembly,
      fill = category
    ),
    position = position_stack(reverse = TRUE),
    data = dat,
    stat="identity",
    width=my_bar_height
  ) +
  coord_flip() +
  theme_gray(base_size = 8) +
  scale_x_discrete(limits = rev(levels(dat$assembly))) +
  scale_y_continuous(labels = c("0","20","40","60","80","100"), breaks = c(0,20,40,60,80,100)) +
  scale_fill_manual(
    values = my_colors,
    labels =c(" Complete (C) and single-copy (S)  ",
              " Complete (C) and duplicated (D)",
              " Fragmented (F)  ",
              " Missing (M)")
  ) +
  ggtitle(my_title) +
  xlab("") +
  ylab("\n%BUSCOs") +
  theme(

    # Plot title
    plot.title = element_text(
      family=my_family,
      hjust=0.5,
      colour = "black",
      size = rel(2.2)*my_size_ratio,
      face = "bold"
    ),

    # Legend information
    legend.position = "top", legend.title = element_blank(),
    legend.text = element_text(family=my_family, size = rel(1.2) * my_size_ratio),

    # Panel
    panel.background = element_rect(color="#FFFFFF", fill="white"),
    panel.grid.minor = element_blank(),
    panel.grid.major = element_blank(),

    # Axis text
    axis.text.y = element_text(family = my_family, colour = "black", size = rel(1.66) * my_size_ratio),
    axis.text.x = element_text(family=my_family, colour = "black", size = rel(1.66) * my_size_ratio),

    # Axis lines (x and y)
    axis.line = element_line(size = 1 * my_size_ratio, colour = "black"),

    # Axis ticks
    axis.ticks.y = element_line(colour="white", size = 0),
    axis.ticks.x = element_line(colour="#222222"),
    axis.ticks.length = unit(0.4, "cm"),

    # Axis titles
    axis.title.x = element_text(family = my_family, size = rel(1.2)*my_size_ratio)
  ) +
  guides(fill = guide_legend(override.aes = list(colour = NULL))) +
  guides(fill = guide_legend(nrow=2, byrow = TRUE))

print('4. Figure created')

# ------------------------------------------------------------------------------------------------ #
# Overlay the count information onto the columns (not a fan but works)
for( i in rev(1:length(unique(dat$assembly)))) {
  s <- rev(unique(dat$summary))[i]

  fig <- fig +
    annotate(
      'text',
      label = s,
      y = 3,
      x = i,
      size = labsize * 5 * my_size_ratio,
      colour = "black",
      hjust = 0,
      family = my_family
    )
}

print('5. Overlaying BUSCO summaries to figure')

# ------------------------------------------------------------------------------------------------ #
# Save the plot
agg_png(
  filename = my_ouput,
  width = my_width,
  height = my_height,
  units = my_unit,
  res = 144
)
print(fig)
invisible(dev.off())

print("6. Figure saved. All done!")
