from typing import final
from os import makedirs
from os import walk
import argparse
import pandas
import matplotlib.pyplot

MARKERS: final = ['X', 'o', 's', 'v', '^', 'P', '*', 'D']
METRIC_TABLE: final = ['', 'K', 'M', 'G', 'T', 'P', 'E']


def format_byte_value(value):
    formatted_value = value
    counter = 0

    while formatted_value >= 1024 and formatted_value != 0 and counter < len(METRIC_TABLE) - 1:
        formatted_value /= 1024
        counter += 1

    if counter == 0:
        return '{}'.format(int(formatted_value))
    else:
        return '{} {}i'.format(int(formatted_value), METRIC_TABLE[counter])


def plot(plot_title, data_frame, output_file, left_axis_column, left_axis_title, left_axis_linestyle='solid',
         left_logy=False, right_axis_column=None, right_axis_title=None, right_axis_linestyle='dashed',
         right_logy=False):

    left_values = data_frame.groupby(['Benchmark', 'Size'])[left_axis_column]
    left_medians = left_values.median().unstack().transpose()
    left_deviations = left_values.std().unstack().transpose()

    x_ticks = left_medians.axes[0]
    x_labels = list(map(format_byte_value, x_ticks))

    ax = left_medians.plot(kind='line', colormap='coolwarm', linestyle=left_axis_linestyle, yerr=left_deviations,
                           legend=False, mark_right=False, grid=True, logx=True, logy=left_logy, xticks=x_ticks,
                           xlim=(x_ticks[0],x_ticks[len(x_ticks) - 1]), title=plot_title, figsize=(16, 9))

    ax.legend(loc='upper center')
    ax.set_xlabel('Size in Byte')
    ax.set_ylabel(left_axis_title)

    ax.xaxis.set_tick_params(which='minor', bottom=False)
    ax.set_xticklabels(x_labels)

    for i, line in enumerate(ax.get_lines()):
        line.set_marker(MARKERS[i])

    if right_axis_column is not None:
        right_values = data_frame.groupby(['Benchmark', 'Size'])[right_axis_column]
        right_medians = right_values.median().unstack().transpose()
        right_deviations = right_values.std().unstack().transpose()

        ax = right_medians.plot(ax=ax, colormap='coolwarm', linestyle=right_axis_linestyle, yerr=right_deviations,
                                legend=False, grid=True, logx=True, logy=right_logy, secondary_y=True)

        ax.set_ylabel(right_axis_title)
        ax.xaxis.set_tick_params(which='minor', bottom=False)

        for i, line in enumerate(ax.get_lines()):
            line.set_marker(MARKERS[i])

    matplotlib.pyplot.savefig(output_file)


parser = argparse.ArgumentParser(prog='observatory', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument('-r', '--result', dest='result_dir', required=True, help='Path to the result directory')
parser.add_argument('-o', '--output', dest='output_dir', default="./plot/", help='Output path for the plot files')

args = parser.parse_args()

try:
    makedirs(args.output_dir)
except FileExistsError:
    pass

for path, _, files in walk(args.result_dir):
    for file in files:
        if file.endswith('.csv'):
            data_frame = pandas.read_csv('{}/{}'.format(path, file))
            if 'Throughput' in file:
                plot(file[:-4], data_frame, '{}/Throughput.svg'.format(args.output_dir), 'OperationThroughput',
                     'Throughput in Operations/s', 'dashed', False, 'DataThroughput', 'Throughput in Byte/s', 'solid')
            if 'Latency' in file:
                plot(file[:-4].replace('Latency', 'Average Latency'), data_frame,
                     '{}/AverageLatency.svg'.format(args.output_dir), 'AverageLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', 'Minimum Latency'), data_frame,
                     '{}/MinimumLatency.svg'.format(args.output_dir), 'MinimumLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', 'Maximum Latency'), data_frame,
                     '{}/MaximumLatency.svg'.format(args.output_dir), 'MaximumLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', '50% Latency'), data_frame,
                     '{}/50thLatency.svg'.format(args.output_dir), '50thLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', '95% Latency'), data_frame,
                     '{}/95thLatency.svg'.format(args.output_dir), '95thLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', '99% Latency'), data_frame,
                     '{}/99thLatency.svg'.format(args.output_dir), '99thLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', '99.9% Latency'), data_frame,
                     '{}/999thLatency.svg'.format(args.output_dir), '999thLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
                plot(file[:-4].replace('Latency', '99.99% Latency'), data_frame,
                     '{}/9999thLatency.svg'.format(args.output_dir), '9999thLatency', 'Latency in s', 'solid', True,
                     'OperationThroughput', 'Throughput in Operations/s', 'dashed')
            if 'DataOverhead' in data_frame.columns:
                plot(file[:-4] + ' Data Overhead', data_frame,
                     '{}/Throughput Data Overhead.svg'.format(args.output_dir), 'DataOverhead',
                     'Data Overhead in Byte')
                plot(file[:-4] + ' Data Overhead Factor', data_frame,
                     '{}/Throughput Data Overhead Factor.svg'.format(args.output_dir), 'DataOverheadFactor',
                     'Data Overhead Factor')
                plot(file[:-4] + ' Overhead', data_frame,
                     '{}/Throughput Overhead.svg'.format(args.output_dir), 'DataThroughputOverhead',
                     'Data Overhead Throughput in Byte/s')