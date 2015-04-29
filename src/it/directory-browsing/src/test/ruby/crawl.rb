require 'net/http'
require 'fileutils'
class Crawl

  attr_accessor :target, :base_url

  def load(path)
    target_file = File.join(@target, File.basename(path))
    target_file = "#{target_file}-index.html" if path =~ /\/$/

                                                         
    FileUtils.mkdir_p(File.dirname(target_file))
    File.open(target_file, 'w') do |f|
      res = Net::HTTP.get_response(URI.parse("#{base_url}/#{path}"))
      if (res.code == '200')
          f.print res.body
      end
      f.close
    end
    FileUtils.rm(target_file) if File.size(target_file) == 0
  end
end
